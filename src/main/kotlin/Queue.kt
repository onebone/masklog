package me.onebone.masklog

data class Queue (
	val location: RegisteredLocation,
	val callback: (stores: List<Store>) -> Unit
)
