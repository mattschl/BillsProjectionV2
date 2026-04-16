package ms.mattschlenkrich.billsprojectionv2.ui.navigation

sealed class Screen(val route: String) {
    object BudgetView : Screen("budget_view")
    object Transactions : Screen("transactions")
    object Accounts : Screen("accounts")
    object Analysis : Screen("analysis")
    object BudgetRules : Screen("budget_rules")
    object Help : Screen("help")
    object Settings : Screen("settings")
    object BudgetList : Screen("budget_list")

    // Detailed screens that might need arguments later
    object AccountAdd : Screen("account_add")
    object AccountUpdate : Screen("account_update")
    object TransactionAdd : Screen("transaction_add")
    object TransactionUpdate : Screen("transaction_update")
    object TransactionPerform : Screen("transaction_perform")
    object TransactionSplit : Screen("transaction_split")
    object BudgetItemAdd : Screen("budget_item_add")
    object BudgetItemUpdate : Screen("budget_item_update")
    object BudgetRuleAdd : Screen("budget_rule_add")
    object BudgetRuleUpdate : Screen("budget_rule_update")
    object AccountTypes : Screen("account_types")
    object AccountTypeAdd : Screen("account_type_add")
    object AccountTypeUpdate : Screen("account_type_update")
    object AccountChoose : Screen("account_choose")
    object BudgetRuleChoose : Screen("budget_rule_choose")
    object Calculator : Screen("calculator")
}