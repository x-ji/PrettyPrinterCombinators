-- From http://homepages.inf.ed.ac.uk/wadler/papers/prettier/prettier.pdf
-- This is the version described in section 1
module PrettyPrinterSimplified where

(<>) :: Doc -> Doc -> Doc
nil :: Doc
text :: String -> Doc
line :: Doc
nest :: Int -> Doc -> Doc
layout :: Doc -> String

-- A simple algebraic implementation, page 5
data Doc = Nil
         | String `Text` Doc
         | Int `Line` Doc deriving Show

-- Correspondence between constructors and document operators. Not actually a part of the code.
-- Nil = nil
-- s `Text` x = text s <> x
-- i `Line` x = nest i line <> x

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

nest i (s `Text` x) = s `Text` nest i x
nest i (j `Line` x) = (i+j) `Line` nest i x
nest i Nil = Nil

layout (s `Text` x) = s ++ layout x
layout (i `Line` x) = '\n' : copy i ' ' ++ layout x
layout Nil = ""

-- Tree example
data Tree = Node String [Tree]

showTree (Node s ts) = text s <> nest (length s) (showBracket ts)

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
