
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ex.messreview.Screens.AuthScreen
import com.ex.messreview.Screens.HomeScreen
import com.ex.messreview.Screens.ProfileScreen
import com.ex.messreview.Screens.RatingScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ex.messreview_admin.Screens.CatererScreen
import com.ex.messreview_admin.Screens.MessTypeScreen
import com.ex.messreview_admin.Screens.itemEditScreen
import com.ex.messreview_admin.viewmodel.AuthViewModel
import com.ex.messreview_admin.viewmodel.MenuViewModel

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val viewModel: MenuViewModel = viewModel()
    NavHost(navController = navController, startDestination = "auth_screen") {
        composable("auth_screen") {
           AuthScreen(
               navController,authViewModel
            )
        }
        composable("caterer_screen") {
            CatererScreen(
                navController = navController,
                onCatererSelected = { catererName ->
                    // Navigate to mess_type_screen and pass selected caterer name
                    navController.navigate("mess_type_screen/$catererName")
                },authViewModel
            )
        }
        composable("profile_screen") {
            ProfileScreen()
        }
        composable(
            "mess_type_screen/{catererName}",
            arguments = listOf(navArgument("catererName") { type = NavType.StringType })
        ) { backStackEntry ->
            MessTypeScreen(
                navController = navController,
                catererName = backStackEntry.arguments?.getString("catererName") ?: "",
                 viewModel
            )
        }
        composable(
            "home_screen/{catererName}/{messType}",
            arguments = listOf(
                navArgument("catererName") { type = NavType.StringType },
                navArgument("messType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            HomeScreen(
                navController = navController,
                catererName = backStackEntry.arguments?.getString("catererName") ?: "",
                messType = backStackEntry.arguments?.getString("messType") ?: "",
                viewModel
            )

        }
        composable(
            route = "rating_screen/{itemName}/{imageResId}/{itemInfo}",
            arguments = listOf(
                navArgument("itemName") { type = NavType.StringType },
                navArgument("imageResId") { type = NavType.IntType },
                navArgument("itemInfo") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val itemName = backStackEntry.arguments?.getString("itemName")
            val imageResId = backStackEntry.arguments?.getInt("imageResId")
            val itemInfo = backStackEntry.arguments?.getString("itemInfo")
            if (itemName != null && imageResId != null) {
                RatingScreen(itemName = itemName, imageResId = imageResId,itemInfo=itemInfo,viewModel)
            }
        }
        composable(
            route = "item_edit_screen/{dayOfWeek}/{mealTime}/{itemName}/{mess}",
            arguments = listOf(
                navArgument("dayOfWeek") { type = NavType.StringType },
                navArgument("mealTime") { type = NavType.StringType },
                navArgument("itemName") { type = NavType.StringType },
                navArgument("mess") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val dayOfWeek = backStackEntry.arguments?.getString("dayOfWeek") ?: ""
            val mealTime = backStackEntry.arguments?.getString("mealTime") ?: ""
            val itemName = backStackEntry.arguments?.getString("itemName") ?: ""
            val mess = backStackEntry.arguments?.getString("mess") ?: ""
            itemEditScreen(dayOfWeek = dayOfWeek, mealTime = mealTime, itemName = itemName,mess=mess,viewModel)
        }


    }
}
