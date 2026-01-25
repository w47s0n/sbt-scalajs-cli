val sample ="  VITE v5.4.21  ready in 174 ms"
val pattern = ".VITE\\s+v".r
pattern.findFirstIn(sample)
