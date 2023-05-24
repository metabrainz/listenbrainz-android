
object App {
    const val compileSdk = 33
    const val minSdk = 21
    const val targetSdk = 33
    const val versionCode = 23
    const val versionName = "1.4.1"
    object namespace {
        const val appNamespace = "org.listenbrainz.android"
        const val sharedTestNamespace = "org.listenbrainz.sharedtest"
    }
    const val testInstrumentationRunner = "org.listenbrainz.android.CustomTestRunner" //"androidx.test.runner.AndroidJUnitRunner"
    const val composeCompiler = "1.4.6"
}


object Versions {
    const val gradle = "8.0.1"
    const val kotlin = "1.8.20"
    
    object compose {
        const val compose_version = "1.4.2"
        const val navigation = "2.6.0-beta01"
    }
    
    const val hilt = "2.45"
    
    object androidx {
        const val core = "1.10.1"
    }
    /*
    def kotlin_version = '1.8.20'
    def navigationVersion = '2.5.3'
    def hilt_version = '2.45'
    def compose_version = '1.4.2'
    def room_version = '2.5.1'
    def accompanist_version = '0.30.0'
    def exoplayer_version = '2.18.6'*/
    
    const val navigationVersion = "2.5.3"
    const val room = "2.5.1"
    const val accompanist = "0.30.0"
    const val exoplayer = "2.18.6"
    
    /* test */
    const val junit = "4.13.2"
}

object Libs {
    // TODO: Migrate all libs here.
}

object TestLibs {
    const val junit = "junit:junit:${Versions.junit}"
}