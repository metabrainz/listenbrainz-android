package org.listenbrainz.shared.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.listenbrainz.shared.applicationContext
import org.listenbrainz.shared.model.PermissionStatus
import org.listenbrainz.shared.repository.AppPreferences
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume

class AndroidPermissionManager(
    private val context: Context = applicationContext,
    private val appPreferences: AppPreferences
) : PermissionManager() {

    private val availablePermissions = AndroidPermission.entries.filter {
        Build.VERSION.SDK_INT >= it.spec.minSdk
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val requestCounter = AtomicInteger()

    // Guards status updates and the "requested once" list, so an older, slower check can never
    // overwrite a newer result.
    private val mutex = Mutex()

    init {
        scope.launch { refresh() }
        scope.launch {
            CurrentActivityTracker.resumed.conflate().collect { refresh() }
        }
    }

    override fun permissionsFor(scope: PermissionScope): List<AppPermission> = when (scope) {
        PermissionScope.ONBOARDING -> listOf(AndroidPermission.SEND_NOTIFICATIONS)
        PermissionScope.LISTEN_SUBMISSION -> listOf(
            AndroidPermission.READ_NOTIFICATIONS,
            AndroidPermission.BATTERY_OPTIMIZATION
        )
    }.filter { it in availablePermissions }

    override suspend fun refresh(): Map<AppPermission, PermissionStatus> =
        updateStatuses(availablePermissions)

    private suspend fun updateStatuses(
        permissions: List<AndroidPermission>
    ): Map<AppPermission, PermissionStatus> = mutex.withLock {
        val requested = appPreferences.requestedPermissionsList.get()
        val statuses = permissions.associateWith { statusOf(it, requested) }

        // Marks runtime permissions that were granted before we ever prompted as requested
        val grantedUnmarked = statuses
            .filterValues { it == PermissionStatus.GRANTED }
            .keys
            .mapNotNull { (it.spec as? AndroidPermissionSpec.Runtime)?.manifestPermission }
            .filter { it !in requested }
        if (grantedUnmarked.isNotEmpty()) {
            appPreferences.requestedPermissionsList.set(requested + grantedUnmarked)
        }

        publish(statuses)
    }

    private fun statusOf(permission: AndroidPermission, requested: List<String>): PermissionStatus {
        return when (val spec = permission.spec) {
            is AndroidPermissionSpec.SpecialAccess ->
                if (spec.isGranted(context)) PermissionStatus.GRANTED else PermissionStatus.NOT_GRANTED

            is AndroidPermissionSpec.Runtime -> {
                val granted = ContextCompat.checkSelfPermission(
                    context, spec.manifestPermission
                ) == PackageManager.PERMISSION_GRANTED
                when {
                    granted -> PermissionStatus.GRANTED
                    isPermanentlyDenied(spec.manifestPermission, requested) -> PermissionStatus.PERMANENTLY_DENIED
                    else -> PermissionStatus.NOT_GRANTED
                }
            }
        }
    }

    // shouldShowRequestPermissionRationale is false both before the first request and after a
    // permanent denial, so the persisted "requested once" list is what tells the two apart.
    private fun isPermanentlyDenied(manifestPermission: String, requested: List<String>): Boolean {
        if (manifestPermission !in requested) return false
        val activity = CurrentActivityTracker.activity ?: return false
        return !activity.shouldShowRequestPermissionRationale(manifestPermission)
    }

    private suspend fun markRequested(manifestPermission: String) = mutex.withLock {
        val requested = appPreferences.requestedPermissionsList.get()
        if (manifestPermission !in requested) {
            appPreferences.requestedPermissionsList.set(requested + manifestPermission)
        }
    }

    override suspend fun request(permission: AppPermission): PermissionStatus {
        require(permission is AndroidPermission) { "Not an Android permission: ${permission.id}" }
        when (val spec = permission.spec) {
            is AndroidPermissionSpec.Runtime -> requestRuntime(spec)
            is AndroidPermissionSpec.SpecialAccess -> requestSpecialAccess(spec)
        }
        // Only the requested permission can have changed, others are picked up on the next resume
        return updateStatuses(listOf(permission)).getValue(permission)
    }

    private suspend fun requestRuntime(spec: AndroidPermissionSpec.Runtime) {
        val activity = CurrentActivityTracker.activity as? ComponentActivity ?: return
        if (isPermanentlyDenied(spec.manifestPermission, appPreferences.requestedPermissionsList.get())) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = "package:${context.packageName}".toUri()
            }
            startActivitySafely(activity, intent)
            return
        }
        markRequested(spec.manifestPermission)
        withContext(Dispatchers.Main.immediate) {
            val lifecycle = activity.lifecycle
            if (lifecycle.currentState == Lifecycle.State.DESTROYED)
                return@withContext

            var launcher: ActivityResultLauncher<String>? = null
            var destroyObserver: LifecycleEventObserver? = null
            try {
                suspendCancellableCoroutine { continuation ->
                    // A recreated activity has its own registry, so the result never reaches this
                    // callback; stop waiting and let the resume refresh pick up the outcome.
                    destroyObserver = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_DESTROY && continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }.also(lifecycle::addObserver)
                    launcher = activity.activityResultRegistry.register(
                        "permission_request_${requestCounter.incrementAndGet()}",
                        ActivityResultContracts.RequestPermission()
                    ) {
                        if (continuation.isActive) continuation.resume(Unit)
                    }
                    launcher?.launch(spec.manifestPermission)
                }
            } finally {
                launcher?.unregister()
                destroyObserver?.let(lifecycle::removeObserver)
            }
        }
    }

    private fun requestSpecialAccess(spec: AndroidPermissionSpec.SpecialAccess) {
        val intent = spec.settingsIntent(context)
        // Some devices have no activity to handle the battery optimisation request
        if (intent.action == Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            && intent.resolveActivity(context.packageManager) == null
        ) return
        startActivitySafely(CurrentActivityTracker.activity, intent)
    }

    private fun startActivitySafely(activity: Activity?, intent: Intent) {
        if (activity != null) {
            activity.startActivity(intent)
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
