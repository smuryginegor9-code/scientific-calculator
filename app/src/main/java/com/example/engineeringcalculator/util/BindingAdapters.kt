package com.example.engineeringcalculator.util

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.example.engineeringcalculator.domain.CalculatorError

@BindingAdapter("calculatorError")
fun setCalculatorError(textView: TextView, error: CalculatorError?) {
    textView.text = error?.let { textView.context.getString(it.toMessageRes()) } ?: ""
}

@BindingAdapter("visibleIfError")
fun setVisibleIfError(view: View, error: CalculatorError?) {
    view.isVisible = error != null
}
