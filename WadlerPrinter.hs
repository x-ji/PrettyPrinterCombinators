-- Code from http://homepages.inf.ed.ac.uk/wadler/papers/prettier/prettier.pdf
module PrettyPrinter where
-- The pretty printer

infixr 5 :<|>
infixr 6 :<>
infixr 6 <>

-- This is the version defined for efficiency.
data DOC = NIL
         | DOC :<> DOC
         | NEST Int DOC
         | TEXT String
         | LINE
         | DOC :<|> DOC

-- This is the crude version
data Doc = Nil
         | String `Text` Doc
         | Int `Line` Doc

nil = NIL
x <> y = x :<> y
-- nest i x = NEST i x
nest = NEST
-- text s = TEXT s
text = TEXT
line = LINE

group x = flatten x :<|> x

flatten :: DOC -> DOC
flatten NIL = NIL
-- We'll just flatten both sides separately and then concatenate them together.
flatten (x :<> y) = flatten x :<> flatten y
-- Have to preserve the indentation here.
flatten (NEST i x) = NEST i (flatten x)
-- It's already a text, nothing to flatten here.
flatten (TEXT s) = TEXT s
-- Flatten a new line to a single space.
flatten LINE = TEXT " "
-- If x and y can be unioned they're the same thing. Just flatten one of them is enough.
flatten (x :<|> y) = flatten x

layout Nil = ""

-- `s` is already a string. No need to do extra operation on it. Just concatenate the result of `layout x`
layout (s `Text` x) = s ++ layout x
-- This is the typical operation of doing layout of an indented new line.
layout (i `Line` x) = '\n' : copy i ' ' ++ layout x

copy i x = [x | _ <- [1..i] ]

-- Automatically choose the best layout according to width and previous number of chars.
-- We convert the document fed to us to a starting list of 0 indentation and the document itself
best w k x = be w k [(0,x)]

-- This function acts on a list indentation-document pairs.
-- The point of a "list" is to produce a single `document` by using `fold` on the list.
be w k [] = Nil
-- If this element of the list has a NIL document then of course we just ignore it and continue to deal with the next element in the list.
be w k ((i,NIL):z) = be w k z
-- If this element is a concatenation of two documents then we can just apply the law and treat them as two separate elements.
be w k (( i,x :<> y) : z ) = be w k ((i, x): (i, y) : z)
-- If the document itself is nested by further j spaces, we just convert it to i + j indentations.
be w k ((i, NEST j x) : z) = be w k ((i+j, x) : z)
-- We know that the `document` formed from `s` will take `length s` characters, so we add it to the argument k
be w k ((i, TEXT s):z) = s `Text` be w (k + length s) z
-- If this element is actually a union of two documents, we'll just try to apply `be` to both of the unioned documents and see which result is "better".
be w k ((i, x :<|> y):z) = better w k (be w k ((i,x):z)) (be w k ((i,y):z))

-- depends on the length of the first document here.
better w k x y = if fits (w-k) x then x else y

-- Seems to be a guard here.
fits w x | w < 0 = False
-- Starts pattern matching on the data type.
fits w Nil = True
-- If it's constructor Text then we'll sub in the length of the `s` part of this `Text`
fits w (s `Text` x) = fits (w - length s) x
-- If it's constructor Line then of course no problem since it won't add any width.
fits w (i `Line` x) = True

pretty w x = layout (best w 0 x)

-- Finish major parts of the printer.

-- Utility functions
-- Concatenate two Doc together, with a space in between
x <+> y = x <> text " " <> y
-- Concatenate two Doc with a line in between.
x </> y = x <> line <> y

-- Just a generic `fold` function defined over `DOC`
folddoc f [] = nil
folddoc f [x] = x
folddoc f (x:xs) = f x (folddoc f xs)

spread = folddoc (<+>)
stack = folddoc (</>)

-- Automatically produce indentation for content with brackets.
bracket l x r = group (text l <>
                  nest 2 (line <> x) <>
                  line <> text r)

x <+/> y = x <> (text " " :<|> line) <> y

-- Fill a line with as many words as possible.
fillwords = folddoc (<+/>) . map text . words

-- Automatically determine whether to put a space or a newline between two documents.
fill [] = nil
fill [x] = x
fill (x:y:zs) = (flatten x <+> fill (flatten y : zs))
                :<|>
                (x </> fill (y : zs))

-- Tree example

data Tree = Node String [Tree]

showTree (Node s ts) = group (text s <> nest (length s) (showBracket ts))

showBracket [] = nil
showBracket ts = text "[" <> nest 1 (showTrees ts) <> text "]"

showTrees [t] = showTree t
showTrees (t:ts) = showTree t <> text "," <> line <> showTrees ts

showTree' (Node s ts) = text s <> showBracket' ts

-- This version doesn't put new lines
showBracket' [] = nil
showBracket' ts = bracket "[" (showTrees' ts) "]"

-- There seemed to be a mistake in the original document? Should be a ' here?
showTrees' [t] = showTree' t
showTrees' (t:ts) = showTree' t <> text "," <> line <> showTrees' ts

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

testtree w = putStr (pretty w (showTree tree))
testtree' w = putStr (pretty w (showTree' tree))

-- XML example
data XML = Elt String [Att] [XML]
         | Txt String

data Att = Att String String

showXML x = folddoc (<>) (showXMLs x)

showXMLs (Elt n a []) = [text "<" <> showTag n a <> text "/>"]
showXMLs (Elt n a c) = [text "<" <> showTag n a <> text ">" <>
                        showFill showXMLs c <>
                        text "</" <> text n <> text ">"]
showXmls (Txt s) = map text (words s)

showAtts (Att n v) = [text n <> text "=" <> text (quoted v)]

quoted s = "\"" ++ s ++ "\""

showTag n a = text n <> showFill showAtts a

showFill f [] = nil
showFill f xs = bracket "" (fill (concat (map f xs))) ""

xml = Elt "p" [
        Att "color" "red",
        Att "font" "Times",
        Att "size" "10"
      ] [
        Txt "Here is some",
        Elt "em" [] [
        Txt "emphasized"
      ],
        Txt "text.",
        Txt "Here is a",
        Elt "a" [
        Att "href" "http://www.eg.com/"
      ] [
        Txt "link"
        ],
        Txt "elsewhere."
      ]

testXML w = putStr (pretty w (showXML xml))
