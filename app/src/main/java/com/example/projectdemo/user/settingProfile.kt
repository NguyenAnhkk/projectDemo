import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.projectdemo.ui.theme.AuthViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun settingProfile(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val database = Firebase.database
    val myRef = database.getReference("users") // Adjust "users" to your desired database path
    var text by remember {
        mutableStateOf("")
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = text,
            onValueChange = { newText -> text = newText },
            label = { Text("Enter your name!") }
        )

        Button(
            onClick = {
                if (text.isNotBlank()) {
                    myRef.child("name").setValue(text)
                    text = "" // Clear the text field after submission if needed
                }
            }
        ) {
            Text(text = "Submit")
        }
    }
}
