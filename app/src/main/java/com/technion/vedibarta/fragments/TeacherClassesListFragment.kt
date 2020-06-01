package com.technion.vedibarta.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.technion.vedibarta.POJOs.Class
import com.technion.vedibarta.POJOs.Filled
import com.technion.vedibarta.POJOs.Unfilled

import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.ClassesListAdapter
import com.technion.vedibarta.data.viewModels.TeacherClassListViewModel
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.changeStatusBarColor
import com.technion.vedibarta.utilities.missingDetailsDialog
import kotlinx.android.synthetic.main.fragment_teacher_classes_list.*

/**
 * A simple [Fragment] subclass.
 */
class TeacherClassesListFragment : Fragment() {

    private val viewModel: TeacherClassListViewModel by viewModels()

    companion object{
        @JvmStatic
        fun newInstance() = TeacherClassesListFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_teacher_classes_list, container, false)
        val classList = v.findViewById<RecyclerView>(R.id.classList)
        classList.isNestedScrollingEnabled = false
        classList.layoutManager = LinearLayoutManager(activity)
        classList.adapter = ClassesListAdapter(
            { onAddClassButtonClick() },
            { itemView: View -> onClassLongPress(itemView) },
            { itemView: View -> onClassClick(itemView) },
            viewModel.classesList
        )
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appCompat = requireActivity() as AppCompatActivity
        appCompat.setSupportActionBar(toolbar)
        toggleCustomToolbar()

    }

    private fun toggleCustomToolbar() {
        val appCompat = requireActivity() as AppCompatActivity
        if (viewModel.itemActionBarEnabled) {
            toolbar.menu.clear()
            appCompat.menuInflater.inflate(R.menu.item_actions_menu, toolbar.menu)
            appCompat.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_clear_white)
            appCompat.supportActionBar?.setDisplayShowHomeEnabled(true)
            appCompat.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            changeStatusBarColor(
                requireActivity(),
                ContextCompat.getColor(requireContext(), android.R.color.black)
            )
        } else {
            toolbar.menu.clear()
            appCompat.menuInflater.inflate(R.menu.chat_search_menu, toolbar.menu)
            appCompat.supportActionBar?.setDisplayShowHomeEnabled(false)
            appCompat.supportActionBar?.setDisplayHomeAsUpEnabled(false)
            appCompat.supportActionBar?.setTitle(R.string.app_name)
            toolbar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            changeStatusBarColor(
                requireActivity(),
                ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
            )
        }
    }

    private fun onAddClassButtonClick() {
        val dialog = MaterialDialog(requireContext())
        dialog.cornerRadius(20f)
            .noAutoDismiss()
            .positiveButton(R.string.create) {
                validateClassDetails().let { result ->
                    when (result) {
                        is Success -> {
                            viewModel.classesList.add(result.cls)
                            //TODO save the new class in database
                            viewModel.chosenClassName = Unfilled
                            viewModel.chosenClassDescription = Unfilled
                            //TODO change to default photo
                            viewModel.chosenClassPicture = null
                            it.dismiss()
                        }
                        is Failure -> {
                            missingDetailsDialog(requireContext(), result.msg)
                        }
                    }
                }
            }
            .negativeButton(R.string.cancel) { it.dismiss() }
            .customView(R.layout.fragment_add_class_dialog)
            .show {
                this.findViewById<TextInputEditText>(R.id.className)
                    .doOnTextChanged { text, _, _, _ ->
                        if (text.isNullOrEmpty())
                            viewModel.chosenClassName = Unfilled
                        else {
                            viewModel.chosenClassName = Filled(text.toString())
                        }
                    }

                this.findViewById<TextInputEditText>(R.id.classDescription)
                    .doOnTextChanged { text, _, _, _ ->
                        if (text.isNullOrEmpty())
                            viewModel.chosenClassDescription = Unfilled
                        else {
                            viewModel.chosenClassDescription = Filled(text.toString())
                        }
                    }
                //TODO Add camera Button click listener and load default photo for class
            }
    }

    private fun validateClassDetails(): ClassesResult {
        val name = when (val name = viewModel.chosenClassName) {
            is Unfilled -> return Failure("")
            is Filled -> name.text
        }
        val description = when (val desc = viewModel.chosenClassDescription) {
            is Unfilled -> return Failure("")
            is Filled -> desc.text
        }

        val photo = viewModel.chosenClassPicture
        //TODO Add Teacher ID to the class
        return Success(Class(name=name, description = description, photo = photo))
    }

    private fun onClassLongPress(v: View): Boolean {
        viewModel.itemActionBarEnabled = true
        toggleCustomToolbar()
        (v as MaterialCardView).isChecked = true
        return true
    }

    private fun onClassClick(v: View): Boolean {
        return true
    }

}

sealed class ClassesResult
data class Success(val cls: Class) : ClassesResult()
data class Failure(val msg: String) : ClassesResult()
