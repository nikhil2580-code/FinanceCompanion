package com.nikhilkhairnar.financecompanion.ui.transactions



import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nikhilkhairnar.financecompanion.data.model.Category
import com.nikhilkhairnar.financecompanion.data.model.Transaction
import com.nikhilkhairnar.financecompanion.data.model.TransactionType
import com.nikhilkhairnar.financecompanion.databinding.FragmentAddEditTransactionBinding
import com.nikhilkhairnar.financecompanion.utils.DateUtils
import com.nikhilkhairnar.financecompanion.utils.hideKeyboard
import com.nikhilkhairnar.financecompanion.utils.shakeError
import com.nikhilkhairnar.financecompanion.utils.snack
import com.nikhilkhairnar.financecompanion.viewmodel.TransactionViewModel
import java.util.*

class AddEditTransactionFragment : Fragment() {

    private var _binding: FragmentAddEditTransactionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionViewModel by activityViewModels()
    private val args: AddEditTransactionFragmentArgs by navArgs()

    // State
    private var selectedType = TransactionType.EXPENSE
    private var selectedCategory = Category.OTHER
    private var selectedDate = System.currentTimeMillis()
    private var existingTransaction: Transaction? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTypeToggle()
        setupCategoryDropdown()
        setupDatePicker()
        setupToolbar()
        setupSaveButton()
        loadExistingTransaction()
    }

    // ─── Toolbar ─────────────────────────────────────────────────

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    // ─── Type Toggle ─────────────────────────────────────────────

    private fun setupTypeToggle() {
        updateTypeUI(TransactionType.EXPENSE) // default

        binding.btnIncome.setOnClickListener {
            selectedType = TransactionType.INCOME
            updateTypeUI(TransactionType.INCOME)
        }

        binding.btnExpense.setOnClickListener {
            selectedType = TransactionType.EXPENSE
            updateTypeUI(TransactionType.EXPENSE)
        }
    }

    private fun updateTypeUI(type: TransactionType) {
        val incomeActive  = type == TransactionType.INCOME
        val expenseActive = type == TransactionType.EXPENSE

        binding.btnIncome.apply {
            setBackgroundColor(
                if (incomeActive)
                    resources.getColor(com.nikhilkhairnar.financecompanion.R.color.income_green, null)
                else
                    resources.getColor(android.R.color.transparent, null)
            )
            setTextColor(
                resources.getColor(
                    if (incomeActive) com.nikhilkhairnar.financecompanion.R.color.surface_white
                    else com.nikhilkhairnar.financecompanion.R.color.text_secondary, null
                )
            )
        }

        binding.btnExpense.apply {
            setBackgroundColor(
                if (expenseActive)
                    resources.getColor(com.nikhilkhairnar.financecompanion.R.color.expense_red, null)
                else
                    resources.getColor(android.R.color.transparent, null)
            )
            setTextColor(
                resources.getColor(
                    if (expenseActive) com.nikhilkhairnar.financecompanion.R.color.surface_white
                    else com.nikhilkhairnar.financecompanion.R.color.text_secondary, null
                )
            )
        }
    }

    // ─── Category Dropdown ───────────────────────────────────────

    private fun setupCategoryDropdown() {
        val categories = Category.entries.toTypedArray()
        val displayNames = categories.map { "${it.emoji} ${it.displayName}" }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            displayNames
        )
        binding.acvCategory.setAdapter(adapter)

        // Default selection
        binding.acvCategory.setText(
            "${selectedCategory.emoji} ${selectedCategory.displayName}",
            false
        )

        binding.acvCategory.setOnItemClickListener { _, _, position, _ ->
            selectedCategory = categories[position]
        }
    }

    // ─── Date Picker ─────────────────────────────────────────────

    private fun setupDatePicker() {
        // Show today's date by default
        binding.etDate.setText(DateUtils.formatDate(selectedDate))

        binding.etDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDate
        }
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(year, month, day)
                selectedDate = calendar.timeInMillis
                binding.etDate.setText(DateUtils.formatDate(selectedDate))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            // Prevent future dates
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    // ─── Load Existing (Edit Mode) ───────────────────────────────

    private fun loadExistingTransaction() {
        val transactionId = args.transactionId
        if (transactionId == -1) {
            // Add mode
            binding.tvScreenTitle.text = "Add Transaction"
            binding.btnDelete.visibility = View.GONE
            return
        }

        // Edit mode — observe all transactions and find the one we need
        binding.tvScreenTitle.text = "Edit Transaction"
        binding.btnDelete.visibility = View.VISIBLE

        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            val transaction = transactions.find { it.id == transactionId }
            transaction?.let { t ->
                existingTransaction = t
                populateFields(t)
                // Only fill once
                viewModel.allTransactions.removeObservers(viewLifecycleOwner)
            }
        }

        binding.btnDelete.setOnClickListener {
            existingTransaction?.let { showDeleteDialog(it) }
        }
    }

    private fun populateFields(transaction: Transaction) {
        binding.etAmount.setText(transaction.amount.toString())
        binding.etTitle.setText(transaction.title)
        binding.etNote.setText(transaction.note)

        selectedType = transaction.type
        updateTypeUI(transaction.type)

        selectedCategory = transaction.category
        binding.acvCategory.setText(
            "${transaction.category.emoji} ${transaction.category.displayName}",
            false
        )

        selectedDate = transaction.date
        binding.etDate.setText(DateUtils.formatDate(transaction.date))
    }

    // ─── Save ────────────────────────────────────────────────────

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (validateForm()) saveTransaction()
        }
    }

    // Replace validateForm() with this improved version
    private fun validateForm(): Boolean {
        var isValid = true

        val amount = binding.etAmount.text.toString().toDoubleOrNull()
        val title  = binding.etTitle.text.toString().trim()

        if (amount == null || amount <= 0) {
            binding.etAmount.shakeError()
            binding.etAmount.error = "Enter a valid amount"
            isValid = false
        } else {
            binding.etAmount.error = null
        }

        if (title.isEmpty()) {
            binding.etTitle.shakeError()
            binding.etTitle.error = "Title is required"
            isValid = false
        } else {
            binding.etTitle.error = null
        }

        return isValid
    }

    private fun saveTransaction() {
        // Disable button + show saving state
        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Saving..."

        val amount = binding.etAmount.text.toString().toDouble()
        val title  = binding.etTitle.text.toString().trim()
        val note   = binding.etNote.text.toString().trim()

        val transaction = Transaction(
            id       = existingTransaction?.id ?: 0,
            amount   = amount,
            type     = selectedType,
            category = selectedCategory,
            title    = title,
            note     = note,
            date     = selectedDate
        )

        if (existingTransaction == null) {
            viewModel.insert(transaction)
            requireView().snack("Transaction added ✅")
        } else {
            viewModel.update(transaction)
            requireView().snack("Transaction updated ✅")
        }

        hideKeyboard()
        findNavController().navigateUp()
    }

    // ─── Delete Dialog ───────────────────────────────────────────

    private fun showDeleteDialog(transaction: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Delete \"${transaction.title}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.delete(transaction)
                findNavController().navigateUp()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}