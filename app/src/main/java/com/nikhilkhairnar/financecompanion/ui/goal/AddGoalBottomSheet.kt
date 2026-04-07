package com.nikhilkhairnar.financecompanion.ui.goal


import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.utils.DateUtils
import com.nikhilkhairnar.financecompanion.databinding.DialogAddGoalBinding
import java.util.*

class AddGoalBottomSheet(
    private val onGoalCreated: (
        title: String,
        targetAmount: Double,
        deadline: Long?,
        emoji: String
    ) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogAddGoalBinding? = null
    private val binding get() = _binding!!

    private var selectedDeadline: Long? = null
    private var selectedEmoji = "🎯"

    // Available emoji options
    private val emojiList = listOf(
        "🎯", "🏠", "✈️", "🚗", "📱",
        "💍", "🎓", "🏥", "💻", "🌴"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddGoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupEmojiSelector()
        setupDeadlinePicker()
        setupCreateButton()
    }

    // ─── Emoji Selector ──────────────────────────────────────────

    private fun setupEmojiSelector() {
        emojiList.forEach { emoji ->
            val tv = layoutInflater.inflate(
                android.R.layout.simple_list_item_1,
                binding.llEmojiSelector,
                false
            ) as android.widget.TextView

            tv.text = emoji
            tv.textSize = 28f
            tv.setPadding(12, 8, 12, 8)
            tv.setOnClickListener {
                selectedEmoji = emoji
                highlightSelectedEmoji(tv)
            }

            // Highlight first one by default
            if (emoji == selectedEmoji) highlightSelectedEmoji(tv)

            binding.llEmojiSelector.addView(tv)
        }
    }

    private fun highlightSelectedEmoji(selected: android.widget.TextView) {
        // Reset all
        for (i in 0 until binding.llEmojiSelector.childCount) {
            val child = binding.llEmojiSelector.getChildAt(i)
            child.background = null
        }
        // Highlight selected
        selected.setBackgroundResource(
            com.nikhilkhairnar.financecompanion.R.drawable.bg_circle_light
        )
    }

    // ─── Deadline Picker ─────────────────────────────────────────

    private fun setupDeadlinePicker() {
        binding.etDeadline.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    cal.set(year, month, day)
                    selectedDeadline = cal.timeInMillis
                    binding.etDeadline.setText(
                        com.nikhilkhairnar.financecompanion.utils.DateUtils.formatDate(
                            selectedDeadline!!
                        )
                    )
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).apply {
                // Only allow future dates for deadline
                datePicker.minDate = System.currentTimeMillis()
            }.show()
        }
    }

    // ─── Create ──────────────────────────────────────────────────

    private fun setupCreateButton() {
        binding.btnCreateGoal.setOnClickListener {
            val title  = binding.etGoalTitle.text.toString().trim()
            val amount = binding.etTargetAmount.text.toString().toDoubleOrNull()

            if (title.isEmpty()) {
                binding.etGoalTitle.error = "Please enter a title"
                return@setOnClickListener
            }
            if (amount == null || amount <= 0) {
                binding.etTargetAmount.error = "Please enter a valid amount"
                return@setOnClickListener
            }

            onGoalCreated(title, amount, selectedDeadline, selectedEmoji)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}