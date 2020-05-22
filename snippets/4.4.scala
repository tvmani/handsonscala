@ Array(1, 2, 3, 4, 5).map(i => i * 2) // Multiply every element by 2
res162: Array[Int] = Array(2, 4, 6, 8, 10)

@ Array(1, 2, 3, 4, 5).filter(i => i % 2 == 1) // Keep only elements not divisible by 2
res163: Array[Int] = Array(1, 3, 5)

@ Array(1, 2, 3, 4, 5).take(2) // keep first two elements
res164: Array[Int] = Array(1, 2)

@ Array(1, 2, 3, 4, 5).drop(2) // discard first two elements
res165: Array[Int] = Array(3, 4, 5)

@ Array(1, 2, 3, 4, 5).slice(1, 4) // keep elements from index 1-4
res166: Array[Int] = Array(2, 3, 4)

@ Array(1, 2, 3, 4, 5, 4, 3, 2, 1, 2, 3, 4, 5, 6, 7, 8).distinct // Removes all duplicates
res167: Array[Int] = Array(1, 2, 3, 4, 5, 6, 7, 8)
