package org.listenbrainz.android.ui.screens.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_orange

@Composable
fun OnboardingLoginScreen(onLoginClick: () -> Unit){
    LoginScreenBase(){
        onLoginClick()
    }
}

@Composable
private fun LoginScreenBase(onLoginClick: ()->Unit){
    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
        ){
        Text("Login")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onLoginClick) {
            Text("Login", color = lb_orange)
        }
    }
}

@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
fun LoginScreenPreview(){
    ListenBrainzTheme{
        LoginScreenBase {

        }
    }
}