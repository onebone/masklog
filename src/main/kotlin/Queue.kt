package me.onebone.masklog

data class Queue (
	val location: String,
	val callback: (stores: List<Store>) -> Unit?
)
