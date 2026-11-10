package org.listenbrainz.android.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UILabel
import platform.UIKit.UIView
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

internal fun keyWindow(): UIWindow? = UIApplication.sharedApplication.connectedScenes
    .filterIsInstance<UIWindowScene>()
    .flatMap { it.windows.filterIsInstance<UIWindow>() }
    .firstOrNull { it.isKeyWindow() }

// UIKit has no toast, so this fades a label in over the key window
@OptIn(ExperimentalForeignApi::class)
actual fun showToast(message: String) {
    dispatch_async(dispatch_get_main_queue()) {
        val window = keyWindow() ?: return@dispatch_async
        val label = UILabel().apply {
            text = message
            textColor = UIColor.whiteColor
            font = UIFont.systemFontOfSize(14.0)
            textAlignment = NSTextAlignmentCenter
            numberOfLines = 0
            backgroundColor = UIColor.blackColor.colorWithAlphaComponent(0.8)
            layer.cornerRadius = 18.0
            clipsToBounds = true
            alpha = 0.0
        }
        val (windowWidth, windowHeight) = window.bounds.useContents { size.width to size.height }
        val bottomInset = window.safeAreaInsets.useContents { bottom }
        val textSize = label.sizeThatFits(CGSizeMake(windowWidth - 96, windowHeight))
        val (width, height) = textSize.useContents { width + 32 to height + 20 }
        label.setFrame(CGRectMake((windowWidth - width) / 2, windowHeight - bottomInset - 64 - height, width, height))
        window.addSubview(label)

        UIView.animateWithDuration(0.2, animations = { label.alpha = 1.0 }) { _ ->
            UIView.animateWithDuration(
                duration = 0.3,
                delay = 2.0,
                options = 0u,
                animations = { label.alpha = 0.0 },
                completion = { _ -> label.removeFromSuperview() }
            )
        }
    }
}
