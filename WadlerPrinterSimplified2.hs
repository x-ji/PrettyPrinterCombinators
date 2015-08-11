-- From http://homepages.inf.ed.ac.uk/wadler/papers/prettier/prettier.pdf
-- This is the version described in section 2
module PrettyPrinterSimplified where

(<>) :: Doc -> Doc -> Doc
nil :: Doc
text :: String -> Doc
line :: Doc
nest :: Int -> Doc -> Doc
layout :: Doc -> String

-- Added in section 2
group :: Doc -> Doc
pretty :: Int -> Doc -> String
-- This is represented by the "Union" constructor. Not actually a part of the code
-- (<|>) :: Doc -> Doc -> Doc
flatten :: Doc -> Doc
data Doc = Nil
         | String `Text` Doc
         | Int `Line` Doc
         -- Added in section 2
         | Doc `Union` Doc deriving Show

-- Correspondence between constructors and document operators. Not actually a part of the code.
-- Nil = nil
-- s `Text` x = text s <> x
-- i `Line` x = nest i line <> x
-- x `Union` y = x <|> y

-- Helper function
copy :: Int -> Char -> String
copy i x = [x | _ <- [1..i] ]

-- Actual representations of functions
nil = Nil
text s = s `Text` Nil
line = 0 `Line` Nil

(s `Text` x) <> y = s `Text` (x <> y)
(i `Line` x) <> y = i `Line` (x <> y)
Nil <> y = y
-- Added in section 2
(x `Union` y) <> z = (x <> z) `Union` (y <> z)

nest i (s `Text` x) = s `Text` nest i x
nest i (j `Line` x) = (i+j) `Line` nest i x
nest i Nil = Nil
-- Added in section 2
nest k (x `Union` y) = nest k x `Union` nest k y

layout (s `Text` x) = s ++ layout x
layout (i `Line` x) = '\n' : copy i ' ' ++ layout x
layout Nil = ""

group Nil = Nil
group (i `Line` x) = (" " `Text` flatten x) `Union` (i `Line` x)
group (s `Text` x) = s `Text` group x
group (x `Union` y) = group x `Union` y

flatten Nil = Nil
flatten (i `Line` x) = " " `Text` flatten x
flatten (s `Text` x) = s `Text` flatten x
flatten (x `Union` y) = flatten x

best w k Nil = Nil
best w k (i `Line` x) = i `Line` best w i x
best w k (s `Text` x) = s `Text` best w (k + length s) x
best w k (x `Union` y) = better w k (best w k x) (best w k y)

better w k x y = if fits (w - k) x then x else y

fits w x | w < 0 = False
fits w Nil = True
fits w (s `Text` x) = fits (w - length s) x
fits w (i `Line` x) = True

pretty w x = layout (best w 0 x)

-- Tree example
data Tree = Node String [Tree]

showTree (Node s ts) = group (text s <> nest (length s) (showBracket ts))

showBracket [] = nil
showBracket ts = text "[" <> nest 1 (showTrees ts) <> text "]"

showTrees [t] = showTree t
showTrees (t:ts) = showTree t <> text "," <> line <> showTrees ts

showTree' (Node s ts) = text s <> showBracket' ts
showBracket' [] = nil
showBracket' ts = text "[" <>
                  nest 2 (line <> showTrees' ts) <>
                  line <> text "]"

showTrees' [t] = showTree' t
showTrees' (t:ts) = showTree' t <> text "," <> line <> showTrees' ts

tree :: Tree
tree = Node "aaa" [
         Node "bbbbb" [
           Node "ccc" [],
           Node "dd" []
         ],
         Node "eee" [],
         Node "ffff" [
           Node "gg" [],
           Node "hhh" [],
           Node "ii" []
         ]
       ]
