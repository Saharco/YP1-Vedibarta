package com.technion.vedibarta.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.POJOs.Teacher

import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.ClassMembersListAdapter
import com.technion.vedibarta.adapters.StudentClassAdapter
import com.technion.vedibarta.data.TeacherMeta
import com.technion.vedibarta.data.viewModels.StudentClassViewModel
import com.technion.vedibarta.main.MainActivity
import kotlinx.android.synthetic.main.fragment_classes_list.*
import kotlinx.android.synthetic.main.fragment_classes_list.searchView
import kotlinx.android.synthetic.main.fragment_classes_list.toolbar


class ClassesListFragment : Fragment(), MainActivity.OnBackPressed {

    val viewModel: StudentClassViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_classes_list, container, false)
        val classList = v.findViewById<RecyclerView>(R.id.classList)
        classList.isNestedScrollingEnabled = false
        classList.layoutManager = LinearLayoutManager(activity)
        classList.adapter = StudentClassAdapter(
            { itemView: View -> viewModel.getClassMembers(itemView) },
            viewModel.classesList
        )
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.inflateMenu(R.menu.chat_list_menu)
        searchView.setMenuItem(toolbar.menu.findItem(R.id.search))
        viewModel.event.observe(viewLifecycleOwner) { event ->
            if (!event.handled) {
                when (event) {
                    is StudentClassViewModel.Event.ClassAdded -> classList.adapter?.notifyItemInserted(
                        viewModel.classesList.size
                    )
                    is StudentClassViewModel.Event.HaveNoClasses -> {
                        emptyClassMessage.visibility = View.VISIBLE
                        classList.visibility = View.GONE
                    }
                    is StudentClassViewModel.Event.DisplayError -> Toast.makeText(
                        requireContext(),
                        event.msgResId,
                        Toast.LENGTH_LONG
                    ).show()
                    is StudentClassViewModel.Event.ClassMembersLoaded -> loadClassMembers(event.teacher, event.members)
                    is StudentClassViewModel.Event.ClassAddedFromInvite -> {
                        classList.adapter?.notifyItemInserted(
                            viewModel.classesList.size
                        )
                        //TODO Alert Dialog for Joining
                    }
                    is StudentClassViewModel.Event.AlreadyInClass -> {
                        //TODO Alert Dialog Already in Class
                    }
                }
                event.handled = true
            }
        }
    }

    private fun loadClassMembers(
        teacher: Teacher,
        members: List<Student>
    ) {
        val dialog = MaterialDialog(requireContext())
        dialog.cornerRadius(20f)
            .noAutoDismiss()
            .customView(R.layout.class_member_list_dialog)
            .show {
                val recyclerView = this.findViewById<RecyclerView>(R.id.membersList)
                recyclerView.isNestedScrollingEnabled = false
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = ClassMembersListAdapter(teacher, members)
            }
    }

    override fun onBackPressed(): Boolean {
        if (searchView.isShown) {
            searchView.closeSearch()
            return true
        }
        return false
    }


}
