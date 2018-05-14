package com.example.anweshmishra.kotlinprinterview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.printerview.PrinterView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PrinterView.create(this)
    }
}
