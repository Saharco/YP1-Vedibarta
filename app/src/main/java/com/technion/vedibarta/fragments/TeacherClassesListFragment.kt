package com.technion.vedibarta.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
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
import com.technion.vedibarta.teacher.TeacherMainActivity
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.changeStatusBarColor
import com.technion.vedibarta.utilities.missingDetailsDialog
import kotlinx.android.synthetic.main.activity_teacher_setup.*
import kotlinx.android.synthetic.main.fragment_teacher_classes_list.*
import kotlinx.android.synthetic.main.fragment_teacher_classes_list.toolbar
import kotlinx.android.synthetic.main.fragment_teacher_personal_info.*

/**
 * A simple [Fragment] subclass.
 */
class TeacherClassesListFragment : Fragment(), TeacherMainActivity.OnBackPressed {

    private val viewModel: TeacherClassListViewModel by viewModels()

    companion object {
        @JvmStatic
        fun newInstance() = TeacherClassesListFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_teacher_classes_list, container, false)
        setHasOptionsMenu(true)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun toggleCustomToolbar() {
        val appCompat = requireActivity() as AppCompatActivity
        if (viewModel.itemActionBarEnabled) {
            toolbar.menu.clear()
            appCompat.menuInflater.inflate(R.menu.item_actions_menu, toolbar.menu)
            appCompat.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_clear_white)
            appCompat.supportActionBar?.setDisplayShowHomeEnabled(true)
            appCompat.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.black
                )
            )
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
            toolbar.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
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
                val allowedLetters = getString(R.string.allowed_letters_regex)
                val className = this.findViewById<TextInputEditText>(R.id.className)
                className.doOnTextChanged { text, _, _, _ ->
                    if (text.isNullOrEmpty()) {
                        viewModel.chosenClassName = Unfilled
                    } else {
                        viewModel.chosenClassName = Filled(text.toString())
                        className.error = null
                    }
                }

                val classDesc = this.findViewById<TextInputEditText>(R.id.classDescription)
                classDesc.doOnTextChanged { text, _, _, _ ->
                    if (text.isNullOrEmpty()) {
                        viewModel.chosenClassDescription = Unfilled
                    } else {
                        viewModel.chosenClassDescription = Filled(text.toString())
                        classDesc.error = null
                    }
                }
                //TODO Add camera Button click listener and load default photo for class
            }
    }

    private fun validateClassDetails(): ClassesResult {
        val name = when (val name = viewModel.chosenClassName) {
            is Unfilled -> return Failure(resources.getString(R.string.teacher_class_list_missing_class_name))
            is Filled -> name.text
        }
        val description = when (val desc = viewModel.chosenClassDescription) {
            is Unfilled -> return Failure(resources.getString(R.string.teacher_class_list_missing_class_description))
            is Filled -> desc.text
        }

        val photo = viewModel.chosenClassPicture
        //TODO Add Teacher ID to the class
        return Success(Class(name = name, description = description, photo = photo))
    }

    private fun onClassLongPress(v: View): Boolean {
        val appCompat = requireActivity() as AppCompatActivity
        viewModel.itemActionBarEnabled = true
        viewModel.selectedItems++
        viewModel.selectedItemsList.add(v as MaterialCardView)
        if (viewModel.selectedItems > 1) {
            toolbar.menu.removeItem(R.id.edit)
            appCompat.supportActionBar?.title =
                "${viewModel.selectedItems} ${getString(R.string.multi_item_selected)}"

        } else {
            appCompat.supportActionBar?.title =
                "${viewModel.selectedItems} ${getString(R.string.single_item_selected)}"
        }
        v.isLongClickable = false
        toggleCustomToolbar()
        v.isChecked = true

        return true
    }

    private fun onClassClick(v: View): Boolean {
        val appCompat = requireActivity() as AppCompatActivity
        if (viewModel.itemActionBarEnabled) {
            if ((v as MaterialCardView).isChecked) {
                viewModel.selectedItems--
                viewModel.selectedItemsList.remove(v)
                v.isLongClickable = true
                v.isChecked = false
                if (viewModel.selectedItems == 0) {
                    viewModel.itemActionBarEnabled = false
                    toggleCustomToolbar()
                    return true
                }
            } else {
                viewModel.selectedItems++
                viewModel.selectedItemsList.add(v)
                v.isChecked = true
                v.isLongClickable = false

            }
            if (viewModel.selectedItems == 1) {
                toolbar.menu.clear()
                appCompat.menuInflater.inflate(R.menu.item_actions_menu, toolbar.menu)
                appCompat.supportActionBar?.title =
                    "${viewModel.selectedItems} ${getString(R.string.single_item_selected)}"
            } else {
                toolbar.menu.removeItem(R.id.edit)
                appCompat.supportActionBar?.title =
                    "${viewModel.selectedItems} ${getString(R.string.multi_item_selected)}"
            }
        }

        return true
    }

    override fun onBackPressed(): Boolean {
        if (viewModel.itemActionBarEnabled) {
            viewModel.itemActionBarEnabled = false
            toggleCustomToolbar()
            viewModel.selectedItems = 0
            viewModel.selectedItemsList.forEach {
                it.isLongClickable = true
                it.isChecked = false
            }
            viewModel.selectedItemsList.clear()
            return true
        }
        return false
    }

}

sealed class ClassesResult
data class Success(val cls: Class) : ClassesResult()
data class Failure(val msg: String) : ClassesResult()
