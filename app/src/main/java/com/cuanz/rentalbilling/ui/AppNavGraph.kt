package com.cuanz.rentalbilling.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cuanz.rentalbilling.ui.admin.AdminDashboardScreen
import com.cuanz.rentalbilling.ui.admin.DevicesScreen
import com.cuanz.rentalbilling.ui.admin.MembersScreen
import com.cuanz.rentalbilling.ui.admin.ReportsScreen
import com.cuanz.rentalbilling.ui.admin.SessionsScreen
import com.cuanz.rentalbilling.ui.auth.LoginScreen
import com.cuanz.rentalbilling.ui.member.MemberDashboardScreen

object Routes {
    const val LOGIN = "login"
    const val ADMIN = "admin"
    const val ADMIN_DEVICES = "admin/devices"
    const val ADMIN_MEMBERS = "admin/members"
    const val ADMIN_SESSIONS = "admin/sessions"
    const val ADMIN_REPORTS = "admin/reports"
    const val MEMBER = "member/{memberId}"
    fun member(id: Long) = "member/$id"
}

@Composable
fun AppNavGraph() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onAdmin = { nav.navigate(Routes.ADMIN) },
                onMember = { id -> nav.navigate(Routes.member(id)) }
            )
        }
        composable(Routes.ADMIN) {
            AdminDashboardScreen(
                onDevices = { nav.navigate(Routes.ADMIN_DEVICES) },
                onMembers = { nav.navigate(Routes.ADMIN_MEMBERS) },
                onSessions = { nav.navigate(Routes.ADMIN_SESSIONS) },
                onReports = { nav.navigate(Routes.ADMIN_REPORTS) },
            )
        }
        composable(Routes.ADMIN_DEVICES) { DevicesScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.ADMIN_MEMBERS) { MembersScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.ADMIN_SESSIONS) { SessionsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.ADMIN_REPORTS) { ReportsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.MEMBER) { backStack ->
            val id = backStack.arguments?.getString("memberId")?.toLongOrNull() ?: 0L
            MemberDashboardScreen(memberId = id, onBack = { nav.popBackStack() })
        }
    }
}
