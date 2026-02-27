package com.example.engineeringcalculator.ui

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.engineeringcalculator.EngineeringCalculatorApp
import com.example.engineeringcalculator.R
import com.example.engineeringcalculator.databinding.FragmentCalculatorBinding
import com.example.engineeringcalculator.domain.AngleUnit
import com.example.engineeringcalculator.domain.CalculatorEngine
import com.example.engineeringcalculator.viewmodel.CalculatorViewModel
import com.example.engineeringcalculator.viewmodel.CalculatorViewModelFactory

class CalculatorFragment : Fragment() {

    private var _binding: FragmentCalculatorBinding? = null
    private val binding: FragmentCalculatorBinding
        get() = _binding!!

    private val viewModel: CalculatorViewModel by viewModels {
        CalculatorViewModelFactory(
            owner = this,
            repository = (requireActivity().application as EngineeringCalculatorApp).repository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalculatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = viewModel

        setupToolbar()
        setupAngleToggle()
        setupButtons()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupToolbar() {
        binding.topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_help -> {
                    findNavController().navigate(R.id.action_calculatorFragment_to_helpFragment)
                    true
                }

                R.id.menu_history -> {
                    findNavController().navigate(R.id.action_calculatorFragment_to_historyFragment)
                    true
                }

                else -> false
            }
        }
    }

    private fun setupAngleToggle() {
        binding.angleToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val unit = if (checkedId == R.id.buttonDeg) AngleUnit.DEG else AngleUnit.RAD
            viewModel.setAngleUnit(unit)
        }

        viewModel.angleUnit.observe(viewLifecycleOwner) { unit ->
            val target = if (unit == AngleUnit.DEG) R.id.buttonDeg else R.id.buttonRad
            if (binding.angleToggleGroup.checkedButtonId != target) {
                binding.angleToggleGroup.check(target)
            }
        }
    }

    private fun setupButtons() {
        val tokenButtons = mapOf(
            R.id.btn0 to "0",
            R.id.btn1 to "1",
            R.id.btn2 to "2",
            R.id.btn3 to "3",
            R.id.btn4 to "4",
            R.id.btn5 to "5",
            R.id.btn6 to "6",
            R.id.btn7 to "7",
            R.id.btn8 to "8",
            R.id.btn9 to "9",
            R.id.btnDot to ".",
            R.id.btnComma to ",",
            R.id.btnPlus to "+",
            R.id.btnMinus to "-",
            R.id.btnMul to "*",
            R.id.btnDiv to "/",
            R.id.btnOpenParen to "(",
            R.id.btnCloseParen to ")",
            R.id.btnPower to "^",
            R.id.btnSquare to "²",
            R.id.btnFactorial to "!",
            R.id.btnPercent to "%",
            R.id.btnSin to "sin(",
            R.id.btnCos to "cos(",
            R.id.btnTan to "tan(",
            R.id.btnLn to "ln(",
            R.id.btnLog to "log10(",
            R.id.btnSqrt to "sqrt(",
            R.id.btnRoot to "^(1/",
            R.id.btnPi to CalculatorEngine.PI_LITERAL,
            R.id.btnE to CalculatorEngine.E_LITERAL
        )

        tokenButtons.forEach { (id, token) ->
            binding.root.findViewById<View>(id).setCalcClickListener {
                viewModel.appendToken(token)
            }
        }

        binding.btnDel.setCalcClickListener { viewModel.deleteLast() }
        binding.btnAc.setCalcClickListener { viewModel.clearAll() }
        binding.btnEquals.setCalcClickListener { viewModel.evaluate() }
    }

    private fun View.setCalcClickListener(action: () -> Unit) {
        setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            action()
        }
    }
}
