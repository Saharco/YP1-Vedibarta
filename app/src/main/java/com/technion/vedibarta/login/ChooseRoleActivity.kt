package com.technion.vedibarta.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.technion.vedibarta.POJOs.UserType
import com.technion.vedibarta.R
import com.technion.vedibarta.teacher.TeacherSetupActivity
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_choose_role.*

class ChooseRoleActivity : VedibartaActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_role)

        chooseStudentRoleButton.setOnClickListener {
            userType = UserType.Student
            startActivity(Intent(this, UserSetupActivity::class.java))
        }

        chooseTeacherRoleButton.setOnClickListener {
            userType = UserType.Teacher
            startActivity(Intent(this, TeacherSetupActivity::class.java))
        }
    }
}
