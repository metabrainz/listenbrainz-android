package org.listenbrainz.android.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController

internal fun keyWindow(): UIWindow? = UIApplication.sharedApplication.connectedScenes
    .filterIsInstance<UIWindowScene>()
    .flatMap { it.windows.filterIsInstance<UIWindow>() }
    .firstOrNull { it.isKeyWindow() }

private fun topViewController(): UIViewController? {
    var controller = keyWindow()?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}

/** Opens the system share sheet with [items]. Call on the main thread. */
@OptIn(ExperimentalForeignApi::class)
internal fun presentShareSheet(items: List<Any>) {
    val presenter = topViewController() ?: return
    val shareSheet = UIActivityViewController(activityItems = items, applicationActivities = null)
    // iPad shows the sheet as a popover, which needs an anchor
    shareSheet.popoverPresentationController?.apply {
        sourceView = presenter.view
        presenter.view.bounds.useContents {
            sourceRect = CGRectMake(size.width / 2, size.height / 2, 0.0, 0.0)
        }
    }
    presenter.presentViewController(shareSheet, animated = true, completion = null)
}
