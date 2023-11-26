package com.assignment.mondrodb.mainApp

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.assignment.mondrodb.R
import com.assignment.mondrodb.databinding.ActivityDocumentBinding
import com.assignment.mondrodb.myFragment.DocumentCreationFragment
import com.assignment.mondrodb.myFragment.DocumentOverviewFragment

class DocumentActivity : DashboardSettings() {

    private lateinit var vBinding: ActivityDocumentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vBinding = ActivityDocumentBinding.inflate(layoutInflater)
        setContentView(vBinding.root)

        replaceFragment(DocumentOverviewFragment())

        vBinding.bnvDocumentNavigation.setOnItemSelectedListener {

            when(it.itemId){
                R.id.documentOverview -> replaceFragment(DocumentOverviewFragment())
                R.id.documentCreation -> replaceFragment(DocumentCreationFragment())

                else -> {

                }
            }

            true
        }

    }

    private fun replaceFragment(fragment: Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fl_main_screen, fragment)
        fragmentTransaction.commit()

    }

}