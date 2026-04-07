package com.nikhilkhairnar.financecompanion.ui.transactions

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.snackbar.Snackbar
import com.nikhilkhairnar.financecompanion.R
import com.nikhilkhairnar.financecompanion.data.model.Transaction
import com.nikhilkhairnar.financecompanion.data.model.TransactionType
import com.nikhilkhairnar.financecompanion.databinding.FragmentTransactionsBinding
import com.nikhilkhairnar.financecompanion.utils.snack
import com.nikhilkhairnar.financecompanion.viewmodel.TransactionViewModel

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionViewModel by activityViewModels()
    private lateinit var adapter: TransactionAdapter
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private var currentFilter: TransactionType? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupFilterChips()
        setupShimmer()
        setupFab()
        setupFabScrollBehavior()
        setupSwipeToDelete()
        observeTransactions()
    }

    // ─── FAB Scroll ──────────────────────────────────────────────

    private fun setupFabScrollBehavior() {
        binding.rvTransactions.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    _binding?.let { binding ->
                        if (dy > 10)  binding.fabAdd.shrink()
                        if (dy < -10) binding.fabAdd.extend()
                    }
                }
            }
        )
    }

    // ─── RecyclerView ────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(
            onItemClick = { transaction ->
                val action = TransactionsFragmentDirections
                    .actionTransactionsToAddEdit(transaction.id)
                findNavController().navigate(action)
            },
            onItemLongClick = { transaction ->
                showDeleteDialog(transaction)
                true
            }
        )
        binding.rvTransactions.apply {
            this.adapter = this@TransactionsFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    // ─── Swipe to Delete ─────────────────────────────────────────

    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or
                    ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position    = viewHolder.adapterPosition
                val transaction = adapter.currentList[position]
                viewModel.delete(transaction)
                _binding?.root?.snack(
                    message     = "\"${transaction.title}\" deleted",
                    duration    = Snackbar.LENGTH_LONG,
                    actionLabel = "Undo"
                ) {
                    viewModel.insert(transaction)
                }
            }

            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float,
                actionState: Int,
                isActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val paint = Paint().apply {
                    color = Color.parseColor("#E74C3C")
                }
                if (dX < 0) {
                    canvas.drawRect(
                        itemView.right + dX, itemView.top.toFloat(),
                        itemView.right.toFloat(), itemView.bottom.toFloat(),
                        paint
                    )
                } else {
                    canvas.drawRect(
                        itemView.left.toFloat(), itemView.top.toFloat(),
                        itemView.left + dX, itemView.bottom.toFloat(),
                        paint
                    )
                }
                super.onChildDraw(
                    canvas, recyclerView, viewHolder,
                    dX, dY, actionState, isActive
                )
            }
        }
        ItemTouchHelper(swipeCallback)
            .attachToRecyclerView(binding.rvTransactions)
    }

    // ─── Search ──────────────────────────────────────────────────

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            viewModel.setSearchQuery(text.toString())
        }
    }

    // ─── Filter Chips ────────────────────────────────────────────

    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            currentFilter = when {
                checkedIds.contains(R.id.chip_income)  -> TransactionType.INCOME
                checkedIds.contains(R.id.chip_expense) -> TransactionType.EXPENSE
                else -> null
            }
            applyFilter()
        }
    }

    // ─── Observe Transactions ────────────────────────────────────

    private fun setupShimmer() {
        shimmerLayout = binding.shimmerContainer.root as ShimmerFrameLayout
        shimmerLayout.startShimmer()
    }

    private fun observeTransactions() {
        viewModel.searchResults.observe(viewLifecycleOwner) { transactions ->
            _binding?.let { binding ->
                if (shimmerLayout.isShimmerStarted) {
                    shimmerLayout.stopShimmer()

                    binding.shimmerContainer.root.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            _binding?.shimmerContainer?.root?.visibility = View.GONE
                        }
                        .start()
                }
                applyFilterToList(transactions)
            }
        }
    }

    private fun applyFilter() {
        val current = viewModel.searchResults.value ?: emptyList()
        applyFilterToList(current)
    }

    private fun applyFilterToList(transactions: List<Transaction>) {
        val filtered = when (currentFilter) {
            TransactionType.INCOME  -> transactions.filter { it.type == TransactionType.INCOME }
            TransactionType.EXPENSE -> transactions.filter { it.type == TransactionType.EXPENSE }
            else -> transactions
        }
        adapter.submitList(filtered)
        updateEmptyState(filtered.isEmpty())
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        _binding?.apply {
            layoutEmpty.visibility    = if (isEmpty) View.VISIBLE else View.GONE
            rvTransactions.visibility = if (isEmpty) View.GONE   else View.VISIBLE
        }
    }

    // ─── FAB ─────────────────────────────────────────────────────

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val action = TransactionsFragmentDirections
                .actionTransactionsToAddEdit(-1)
            findNavController().navigate(action)
        }
    }

    // ─── Delete Dialog ───────────────────────────────────────────

    private fun showDeleteDialog(transaction: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete \"${transaction.title}\"?")
            .setPositiveButton("Delete") { _, _ -> viewModel.delete(transaction) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}