package com.example.outkeeper

import java.util.Date

class Person(name:String, var photos: List<String>):java.io.Serializable{
    val name = name
}