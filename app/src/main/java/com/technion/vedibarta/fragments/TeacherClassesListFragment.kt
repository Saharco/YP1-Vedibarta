package com.technion.vedibarta.fragments

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.technion.vedibarta.POJOs.Filled
import com.technion.vedibarta.POJOs.Unfilled
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.ClassMembersListAdapter
import com.technion.vedibarta.adapters.ClassesListAdapter
import com.technion.vedibarta.data.TeacherMeta
import com.technion.vedibarta.data.viewModels.ClassAddViewModel
import com.technion.vedibarta.data.viewModels.TeacherClassListViewModel
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.changeStatusBarColor
import com.technion.vedibarta.utilities.missingDetailsDialog
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_teacher_classes_list.*
import java.io.File

/**
 * A simple [Fragment] subclass.
 */
class TeacherClassesListFragment : Fragment() {

    private val viewModel: TeacherClassListViewModel by viewModels()
    private val classAddViewModel: ClassAddViewModel by viewModels()

    //TODO remove code duplication by moving stuff into functions
    companion object {
        private const val APP_PERMISSION_REQUEST_CAMERA = 100
        private const val REQUEST_CAMERA = 1
        private const val SELECT_IMAGE = 2

        @JvmStatic
        fun newInstance() = TeacherClassesListFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }
        val v = inflater.inflate(R.layout.fragment_teacher_classes_list, container, false)
        setHasOptionsMenu(true)
        val classList = v.findViewById<RecyclerView>(R.id.classList)
        classList.isNestedScrollingEnabled = false
        classList.layoutManager = LinearLayoutManager(activity)
        classList.adapter = ClassesListAdapter(
            { onAddClassButtonClick() },
            { itemView: View -> onClassLongPress(itemView) },
            { itemView: View -> onClassClick(itemView) },
            { itemView: View -> viewModel.createClassInvite(itemView) },
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
                    is TeacherClassListViewModel.Event.ClassAdded -> classList.adapter?.notifyItemInserted(
                        viewModel.classesList.size
                    )
                    is TeacherClassListViewModel.Event.ClassRemoved -> classList.adapter?.notifyItemRemoved(
                        event.index
                    )
                    is TeacherClassListViewModel.Event.ClassEdited -> classList.adapter?.notifyItemChanged(
                        event.index
                    )
                    is TeacherClassListViewModel.Event.ToggleActionBar -> toggleCustomToolbar()
                    is TeacherClassListViewModel.Event.UpdateTitle -> updateSelectedTitle()
                    is TeacherClassListViewModel.Event.DisplayError -> Toast.makeText(
                        requireContext(),
                        event.msgResId,
                        Toast.LENGTH_LONG
                    ).show()
                    is TeacherClassListViewModel.Event.ClassMembersLoaded -> loadClassMembers(event.members)
                    is TeacherClassListViewModel.Event.ClassInviteCreated -> showLinkDialog(event.link)
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
            R.id.edit -> onClassEditClick()
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

    private fun showLinkDialog(link: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, link.toString())

        startActivity(Intent.createChooser(intent, "Share Link"))
//        MaterialDialog(requireContext()).show {
//            input(inputType = InputType.TYPE_NULL, prefill = link.toString()) { dialog, text ->
//            }
//            positiveButton(R.string.copy) {
//                val clipboard: ClipboardManager =
//                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//                val data = ClipData.newUri(context.contentResolver, getString(R.string.class_invite_text), link)
//                clipboard.setPrimaryClip(data)
//            }
//        }
    }

    private fun onClassEditClick() {
        val dialog = MaterialDialog(requireContext())
        dialog.cornerRadius(20f)
            .noAutoDismiss()
            .positiveButton(R.string.save) {
                classAddViewModel.saveEditedClass()

            }
            .negativeButton(R.string.cancel) { it.dismiss() }
            .customView(R.layout.fragment_add_class_dialog)
            .show {
                classAddViewModel.event.removeObservers(viewLifecycleOwner)
                classAddViewModel.event.observe(viewLifecycleOwner) { event ->
                    if (!event.handled) {
                        when (event) {
                            is ClassAddViewModel.ClassAddEvent.ClassEdited -> {
                                viewModel.editClass(event.clazzMap)
                                this.dismiss()
                            }
                            is ClassAddViewModel.ClassAddEvent.StartPictureLoading -> startLoadingClassPicture(
                                this
                            )
                            is ClassAddViewModel.ClassAddEvent.DisplayMissingInfoDialog -> missingDetailsDialog(
                                requireContext(),
                                getString(event.msgResId)
                            )
                            is ClassAddViewModel.ClassAddEvent.FinishPictureLoading -> finishLoadingClassPicture(
                                this
                            )
                        }
                        event.handled = true
                    }
                }
                val className = this.findViewById<TextInputEditText>(R.id.className)
                className.text = SpannableStringBuilder(
                    viewModel.selectedItemsList.first().findViewById<TextView>(R.id.className).text
                )
                classAddViewModel.chosenClassName = Filled(className.text.toString())
                className.doOnTextChanged { text, _, _, _ ->
                    if (text.isNullOrEmpty()) {
                        classAddViewModel.chosenClassName = Unfilled
                    } else {
                        classAddViewModel.chosenClassName = Filled(text.toString())
                    }
                }

                val classDesc = this.findViewById<TextInputEditText>(R.id.classDescription)
                classDesc.text = SpannableStringBuilder(
                    viewModel.selectedItemsList.first()
                        .findViewById<TextView>(R.id.classDescription).text
                )
                classAddViewModel.chosenClassDescription = Filled(classDesc.text.toString())
                classDesc.doOnTextChanged { text, _, _, _ ->
                    if (text.isNullOrEmpty()) {
                        classAddViewModel.chosenClassDescription = Unfilled
                    } else {
                        classAddViewModel.chosenClassDescription = Filled(text.toString())
                    }
                }
                val classPhoto = this.findViewById<CircleImageView>(R.id.classPhoto)
                classPhoto.setImageDrawable(
                    viewModel.selectedItemsList.first()
                        .findViewById<AppCompatImageView>(R.id.classPhoto).drawable
                )
                findViewById<FloatingActionButton>(R.id.addPhotoFab)
                    .setOnClickListener {
                        uploadPhotoForClassHandler()
                    }
            }
    }

    private fun onAddClassButtonClick() {
        val dialog = MaterialDialog(requireContext())
        dialog.cornerRadius(20f)
            .noAutoDismiss()
            .positiveButton(R.string.create) {
                classAddViewModel.createClass()
            }
            .negativeButton(R.string.cancel) { it.dismiss() }
            .customView(R.layout.fragment_add_class_dialog)
            .show {
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
                            is ClassAddViewModel.ClassAddEvent.StartPictureLoading -> startLoadingClassPicture(
                                this
                            )
                            is ClassAddViewModel.ClassAddEvent.FinishPictureLoading -> finishLoadingClassPicture(
                                this
                            )
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
                findViewById<CircleImageView>(R.id.classPhoto)
                    .setImageResource(R.drawable.ic_class_default_photo)
                findViewById<FloatingActionButton>(R.id.addPhotoFab)
                    .setOnClickListener {
                        uploadPhotoForClassHandler()
                    }
            }
    }

    private fun finishLoadingClassPicture(materialDialog: MaterialDialog) {
        materialDialog.findViewById<ProgressBar>(R.id.classPhotoPB).visibility = View.GONE
        materialDialog.findViewById<CircleImageView>(R.id.classPhoto).visibility = View.VISIBLE
        materialDialog.findViewById<FloatingActionButton>(R.id.addPhotoFab).visibility =
            View.VISIBLE
        materialDialog.findViewById<CircleImageView>(R.id.classPhoto)
            .setImageURI(classAddViewModel.chosenClassPicture)
    }

    private fun startLoadingClassPicture(materialDialog: MaterialDialog) {
        materialDialog.findViewById<ProgressBar>(R.id.classPhotoPB).visibility = View.VISIBLE
        materialDialog.findViewById<CircleImageView>(R.id.classPhoto).visibility = View.GONE
        materialDialog.findViewById<FloatingActionButton>(R.id.addPhotoFab).visibility = View.GONE
    }

    private fun uploadPhotoForClassHandler() {
        MaterialDialog(requireContext())
            .negativeButton(R.string.cancel) { it.dismiss() }
            .customView(R.layout.profile_picture_dialog)
            .show {
                val title = findViewById<TextView>(R.id.alertTitle)
                if (TeacherMeta.teacher.gender == Gender.MALE)
                    title.text = getString(R.string.class_choose_picture_title_m)
                else
                    title.text = getString(R.string.class_choose_picture_title_f)

                findViewById<ImageView>(R.id.galleryUploadButton)
                    .setOnClickListener {
                        onGalleryUploadClicked()
                        this.dismiss()
                    }
                findViewById<ImageView>(R.id.cameraUploadButton)
                    .setOnClickListener {
                        onCameraUploadClicked()
                        this.dismiss()
                    }
                findViewById<MaterialButton>(R.id.dismissButton).visibility = View.GONE
            }
    }

    private fun onGalleryUploadClicked() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, SELECT_IMAGE)
    }

    private fun onCameraUploadClicked() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                APP_PERMISSION_REQUEST_CAMERA
            )
        } else {
            startCameraActivity()
        }
    }

    private fun startCameraActivity() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val selectedImageFile = File(
            requireContext().externalCacheDir,
            System.currentTimeMillis().toString() + ".jpg"
        )
        classAddViewModel.chosenClassPicture = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider", selectedImageFile
        )

        intent.putExtra(MediaStore.EXTRA_OUTPUT, classAddViewModel.chosenClassPicture)
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK)
            return

        when (requestCode) {
            REQUEST_CAMERA -> classAddViewModel.uploadImage(
                classAddViewModel.chosenClassPicture,
                requireContext()
            )
            SELECT_IMAGE -> classAddViewModel.uploadImage(data!!.data, requireContext())
        }
    }

    private fun onClassLongPress(v: View): Boolean {
        viewModel.beginClassExtraActions(v as MaterialCardView)
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
            viewModel.getClassMembers(v)
        }

        return true
    }

    private fun loadClassMembers(studentList: List<Student>) {
        val dialog = MaterialDialog(requireContext())
        dialog.cornerRadius(20f)
            .noAutoDismiss()
            .customView(R.layout.class_member_list_dialog)
            .show {
                val recyclerView = this.findViewById<RecyclerView>(R.id.membersList)
                recyclerView.isNestedScrollingEnabled = false
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = ClassMembersListAdapter(TeacherMeta.teacher, studentList)
            }
    }

    fun onBackPressed() {
        if (viewModel.handleOnBackPress())
            findNavController().navigateUp()
    }
}


