package com.nikhilkhairnar.financecompanion.utils


import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.nikhilkhairnar.financecompanion.R

// ── View Visibility ──────────────────────────────────────────────

fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun View.showIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

// ── Animations ───────────────────────────────────────────────────

fun View.animateFadeIn(duration: Long = 300) {
    alpha = 0f
    show()
    animate()
        .alpha(1f)
        .setDuration(duration)
        .start()
}

fun View.animateFadeOut(duration: Long = 300, onEnd: (() -> Unit)? = null) {
    animate()
        .alpha(0f)
        .setDuration(duration)
        .withEndAction {
            hide()
            onEnd?.invoke()
        }
        .start()
}

fun View.animateScaleIn() {
    scaleX = 0f
    scaleY = 0f
    show()
    animate()
        .scaleX(1f)
        .scaleY(1f)
        .setDuration(250)
        .start()
}

fun View.popIn() {
    val anim = AnimationUtils.loadAnimation(context, R.anim.scale_up)
    startAnimation(anim)
}

fun View.shakeError() {
    val shake = ObjectAnimator.ofFloat(
        this, "translationX",
        0f, 16f, -16f, 12f, -12f, 8f, -8f, 0f
    )
    shake.duration = 400
    shake.start()
}

// ── Progress Bar ─────────────────────────────────────────────────

fun ProgressBar.animateTo(targetProgress: Int, duration: Long = 600) {
    val animator = ObjectAnimator.ofInt(this, "progress", progress, targetProgress)
    animator.duration = duration
    animator.start()
}

// ── Keyboard ─────────────────────────────────────────────────────

fun Fragment.hideKeyboard() {
    val imm = requireContext()
        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    requireActivity().currentFocus?.let { view ->
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun Fragment.showKeyboard(view: View) {
    view.requestFocus()
    val imm = requireContext()
        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

// ── Snackbar ─────────────────────────────────────────────────────

fun View.snack(
    message: String,
    duration: Int = Snackbar.LENGTH_SHORT,
    actionLabel: String? = null,
    action: (() -> Unit)? = null
) {
    val snackbar = Snackbar.make(this, message, duration)
    if (actionLabel != null && action != null) {
        snackbar.setAction(actionLabel) { action() }
    }
    snackbar.show()
}

// ── Toast ────────────────────────────────────────────────────────

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}