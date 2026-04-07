💰 FinanceCompanion

A personal finance companion Android app that helps users understand
their daily money habits in a simple and engaging way.

Built as part of a mobile developer internship assignment for **Zorvyn**.

---

## 📱 Screenshots

<img width="720" height="1604" alt="Home" src="https://github.com/user-attachments/assets/7455b25c-232d-4cf4-9dd3-9f0d6a25aa88" />
<img width="720" height="1604" alt="Transactions" src="https://github.com/user-attachments/assets/07e09010-25f5-4407-b2ae-332c4c0b205f" />
<img width="720" height="1604" alt="Add Transaction" src="https://github.com/user-attachments/assets/30002ea5-c051-4a38-b1b6-648d48288d5c" />
<img width="720" height="1604" alt="Goal" src="https://github.com/user-attachments/assets/52c7675f-9fe4-46a8-b278-220d7174e578" />
<img width="720" height="1604" alt="Goal Insight" src="https://github.com/user-attachments/assets/c577ebdd-ba93-470b-8e47-158903917110" />
<img width="720" height="1604" alt="Insights" src="https://github.com/user-attachments/assets/8720089a-0b09-4cf8-8217-dd6d16287bc1" />





---

## ✨ Features

### 🏠 Home Dashboard
- Real-time balance card showing total income, expenses and net balance
- Animated balance counter on load
- Spending by category pie chart (MPAndroidChart)
- Recent 5 transactions with quick glance view
- Dynamic greeting based on time of day

### 💳 Transaction Management
- Add, edit and delete transactions
- Income / Expense type toggle
- 11 spending categories with emoji
- Date picker with future date prevention
- Search transactions by title or note
- Filter by All / Income / Expense chips
- Swipe left or right to delete with Undo
- Shimmer loading animation on first load

### 🎯 Savings Goals
- Create goals with custom emoji, title, target amount and deadline
- Visual progress bar per goal
- Add savings incrementally with dialog
- Auto-marks goal as completed at 100%
- Celebration dialog on goal completion
- Smart alert banner when spending ratio is high

### 📊 Insights
- This month vs last month expense comparison
- Weekly bar chart showing daily spending
- Top 5 spending categories with progress bars
- Quick insight facts:
  - Biggest single expense
  - Most frequent category
  - Savings rate percentage
  - Average daily spend
  - No-spend days this week

### 🎨 UX & Polish
- Smooth fragment transition animations
- Staggered RecyclerView item enter animations
- FAB extends and shrinks on scroll
- Form shake animation on validation error
- Progress bars animate smoothly
- Status bar color changes per screen
- Empty states on all screens
- Ripple touch feedback on all cards
- Keyboard auto-hides after save

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | XML Layouts + ViewBinding |
| Architecture | MVVM |
| Navigation | Jetpack Navigation Component + SafeArgs |
| Database | Room (SQLite) |
| Async | Kotlin Coroutines |
| State | ViewModel + LiveData |
| Charts | MPAndroidChart |
| Loading | Facebook Shimmer |
| DI approach | Manual (constructor injection via Repository) |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 35 (Android 15) |

---

## 🏗️ Architecture

The app follows **MVVM (Model-View-ViewModel)** pattern with a
clean separation of concerns:
┌─────────────────────────────────────────┐
│              UI Layer                   │
│   Fragments + Adapters + XML Layouts    │
└──────────────────┬──────────────────────┘
│ observes
┌──────────────────▼──────────────────────┐
│           ViewModel Layer               │
│   TransactionViewModel, GoalViewModel   │
│         InsightsViewModel               │
└──────────────────┬──────────────────────┘
│ calls
┌──────────────────▼──────────────────────┐
│          Repository Layer               │
│  TransactionRepository, GoalRepository  │
└──────────────────┬──────────────────────┘
│ queries
┌──────────────────▼──────────────────────┐
│           Data Layer                    │
│     Room Database, DAOs, Entities       │
└─────────────────────────────────────────┘

---

## 📁 Project Structure
com.nikhilkhairnar.financecompanion/
│
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt        ← Room singleton
│   │   ├── TransactionDao.kt     ← DB queries
│   │   ├── GoalDao.kt
│   │   └── Converters.kt         ← Enum type converters
│   ├── model/
│   │   ├── Transaction.kt        ← Room entity
│   │   ├── Goal.kt               ← Room entity
│   │   └── Category.kt           ← Enum with emoji
│   └── repository/
│       ├── TransactionRepository.kt
│       └── GoalRepository.kt
│
├── ui/
│   ├── home/
│   │   ├── HomeFragment.kt
│   │   └── RecentTransactionAdapter.kt
│   ├── transactions/
│   │   ├── TransactionsFragment.kt
│   │   ├── AddEditTransactionFragment.kt
│   │   └── TransactionAdapter.kt
│   ├── goals/
│   │   ├── GoalsFragment.kt
│   │   ├── GoalAdapter.kt
│   │   └── AddGoalBottomSheet.kt
│   └── insights/
│       ├── InsightsFragment.kt
│       ├── CategoryInsightAdapter.kt
│       └── InsightFactAdapter.kt
│
├── viewmodel/
│   ├── TransactionViewModel.kt
│   ├── GoalViewModel.kt
│   └── InsightsViewModel.kt
│
├── navigation/
│   └── (nav_graph.xml in res/)
│
└── utils/
├── DateUtils.kt
├── CurrencyUtils.kt
└── Extensions.kt

---

## ⚙️ Setup & Installation

### Prerequisites
- Android Studio Otter 2025.2.3 or newer
- JDK 21
- Android device or emulator (API 26+)

### Steps

1. Clone the repository:
```bash
git clone https://github.com/yourusername/FinanceCompanion.git
```

2. Open in Android Studio:
File → Open → select the FinanceCompanion folder

3. Let Gradle sync complete (requires internet for dependencies)

4. Run the app:
Run → Run 'app'  or  Shift + F10

> No API keys or external services are required.
> All data is stored locally on device using Room database.

---

## 💡 Design Decisions & Assumptions

| Decision | Reason |
|---|---|
| **Local storage only (Room)** | Assignment allows this — no backend required, and it keeps the app fully offline |
| **LiveData over Flow** | Simpler to observe in XML-based Fragments with `viewLifecycleOwner` |
| **`activityViewModels()` for shared state** | Allows Home, Transactions and Goals to share the same ViewModel instance so data stays in sync across tabs |
| **`viewModels()` for Insights** | Insights recomputes on every tab visit — scoping it to the Fragment ensures fresh calculations |
| **INR (₹) as default currency** | Assumed Indian market based on company location |
| **No future dates on transactions** | Prevents incorrect data entry — users log past or current transactions |
| **`fallbackToDestructiveMigration()`** | Used during development only — in production this would be replaced with proper migrations |
| **Category as Enum** | Fixed category set keeps UI consistent and avoids user input complexity |
| **Bottom sheet for goals** | Less disruptive UX than a full screen for a short form |

---

## 🔮 What I Would Add With More Time

- Dark mode toggle with DataStore preference
- Export transactions as CSV
- Biometric lock screen
- Push notifications for goal deadlines
- Multi-currency support
- Recurring transaction templates
- Monthly budget limits per category
- Widget for home screen balance

---

## 👤 Author

**Nikhil Khairnar**
Android Developer
[github.com/nikhilkhairnar]([https://github.com/nikhilkhairnar](https://github.com/nikhil2580-code/FinanceCompanion))

---

## 📄 License

This project was built as an internship assignment submission.
