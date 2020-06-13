package com.technion.vedibarta.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.technion.vedibarta.POJOs.Class
import com.technion.vedibarta.POJOs.Filled
import com.technion.vedibarta.POJOs.Unfilled
import androidx.lifecycle.observe
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.ClassMembersListAdapter
import com.technion.vedibarta.adapters.ClassesListAdapter
import com.technion.vedibarta.data.viewModels.ClassAddViewModel
import com.technion.vedibarta.data.viewModels.TeacherClassListViewModel
import com.technion.vedibarta.teacher.TeacherMainActivity
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.changeStatusBarColor
import com.technion.vedibarta.utilities.missingDetailsDialog
import kotlinx.android.synthetic.main.fragment_teacher_classes_list.*
import kotlinx.android.synthetic.main.fragment_teacher_classes_list.toolbar

/**
 * A simple [Fragment] subclass.
 */
class TeacherClassesListFragment : Fragment(), TeacherMainActivity.OnBackPressed {

    private val viewModel: TeacherClassListViewModel by viewModels()

    //TODO remove code duplication by moving stuff into functions
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
        val classList = view.findViewById<RecyclerView>(R.id.classList)
        viewModel.event.observe(viewLifecycleOwner) { event ->
            if (!event.handled) {
                when (event) {
                    is TeacherClassListViewModel.Event.ClassAdded -> classList.adapter?.notifyItemInserted(viewModel.classesList.size)
                    is TeacherClassListViewModel.Event.ClassRemoved -> classList.adapter?.notifyItemRemoved(event.index)
                    is TeacherClassListViewModel.Event.ToggleActionBar -> toggleCustomToolbar()
                    is TeacherClassListViewModel.Event.UpdateTitle -> updateSelectedTitle()
                    is TeacherClassListViewModel.Event.DisplayError -> Toast.makeText(requireContext(), event.msgResId, Toast.LENGTH_LONG).show()
                }
                event.handled = true
            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
            R.id.delete -> {
                viewModel.removeSelectedClasses()
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
                val classAddViewModel: ClassAddViewModel by viewModels()
                classAddViewModel.createClass()

            }
            .negativeButton(R.string.cancel) { it.dismiss() }
            .customView(R.layout.fragment_add_class_dialog)
            .show {
                val classAddViewModel: ClassAddViewModel by viewModels()
                classAddViewModel.event.removeObservers(viewLifecycleOwner)
                classAddViewModel.event.observe(viewLifecycleOwner) { event ->
                    if (!event.handled) {
                        when (event) {
                            is ClassAddViewModel.ClassAddEvent.ClassAdded -> {
                                viewModel.addClass(event.clazz)
                                this.dismiss()
                            }
                            is ClassAddViewModel.ClassAddEvent.DisplayMissingInfoDialog -> missingDetailsDialog(
                                requireContext(),
                                getString(event.msgResId)
                            )
                            is ClassAddViewModel.ClassAddEvent.DisplayError -> Toast.makeText(
                                requireContext(),
                                event.msgResId,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        event.handled = true
                    }
                }
                val className = this.findViewById<TextInputEditText>(R.id.className)
                className.doOnTextChanged { text, _, _, _ ->
                    if (text.isNullOrEmpty()) {
                        classAddViewModel.chosenClassName = Unfilled
                    } else {
                        classAddViewModel.chosenClassName = Filled(text.toString())
                    }
                }

                val classDesc = this.findViewById<TextInputEditText>(R.id.classDescription)
                classDesc.doOnTextChanged { text, _, _, _ ->
                    if (text.isNullOrEmpty()) {
                        classAddViewModel.chosenClassDescription = Unfilled
                    } else {
                        classAddViewModel.chosenClassDescription = Filled(text.toString())
                    }
                }
                //TODO Add camera Button click listener and load default photo for class
            }
    }

    private fun onClassLongPress(v: View): Boolean {
        viewModel.beginClassEdit(v as MaterialCardView)
        return true
    }

    private fun updateSelectedTitle() {
        val appCompat = requireActivity() as AppCompatActivity
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


    private fun onClassClick(v: View): Boolean {
        if (viewModel.itemActionBarEnabled) {
            if ((v as MaterialCardView).isChecked) {
                viewModel.unSelectClass(v)
            } else {
                viewModel.selectClass(v)
            }
        } else {
            loadClassMembers(v)
        }

        return true
    }

    private fun loadClassMembers(selectedClass: View) {
        val dialog = MaterialDialog(requireContext())
        dialog.cornerRadius(20f)
            .noAutoDismiss()
            .customView(R.layout.class_member_list_dialog)
            .show {
                val recyclerView = this.findViewById<RecyclerView>(R.id.membersList)
                recyclerView.isNestedScrollingEnabled = false
                recyclerView.layoutManager = LinearLayoutManager(context)
                //TODO Change adapter to load members from database
                recyclerView.adapter = ClassMembersListAdapter()
            }
    }

    override fun onBackPressed(): Boolean {
        return viewModel.handleOnBackPress()
    }

}


