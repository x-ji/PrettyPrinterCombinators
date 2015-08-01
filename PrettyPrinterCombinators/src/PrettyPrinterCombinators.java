import java.lang.IndexOutOfBoundsException;
import java.lang.StringBuilder;
import java.lang.reflect.Array;
import java.util.*;


// Not sure if this is the best approach. Just write it here first.


// Well I think normally we don't write all capitals for types in Java. Let's see if there's a better way.
interface DOC1Alg<DOC1> {
  // Data types
  DOC1 Nil1();

  // Have to change method name as :<> is not a valid Java identifier.
  DOC1 CONCAT1(DOC1 left, DOC1 right);

  DOC1 NEST1(Integer i, DOC1 d);

  DOC1 TEXT1(String s);

  DOC1 LINE1();

  // :<|> is not a valid Java identifier
  DOC1 UNION1(DOC1 left, DOC1 right);
}

interface Doc2Alg<Doc2> {
  Doc2 Nil2();

  Doc2 Text2(String s, Doc2 d);

  Doc2 Line2(Integer x, Doc2 d);
}

// This is the original operations of DOC1 type.
interface DOC1 {
  DOC1 flatten();

  Doc2 be(Integer w, Integer k, ArrayList<Pair<Integer, DOC1>> l);

  static Doc2 best(Integer w, Integer k, DOC1 x) {
    ArrayList<Pair<Integer, DOC1>> l = new ArrayList<Pair<Integer, DOC1>>();
    l.add(new Pair<>(0, x));
    return x.be(w, k, l);
  }

  static String copy(Integer i, char c) {
    StringBuilder sb = new StringBuilder();
    for (int count = 0; count < i; count++) {
      sb.append(c);
    }
    return sb.toString();
  }

  static DOC1 group(DOC1 x) {
    UNION1 u = new UNION1(x, x);
    return u.flatten();
  }

  static String pretty(Integer w, DOC1 x) {
    return best(w, 0, x).layout();
  }
}

interface Doc2 {
  String layout();

  Boolean fits(Integer w, Doc2 x);

  //    Doc2 be(int w)
//  Doc2 better();
  static Doc2 better(Integer w, Integer k, Doc2 x, Doc2 y) {
    if (x.fits(w - k, x)) {
      return x;
    } else {
      return y;
    }
  }
}


// Doc2 part
class Nil2 implements Doc2 {
  public Nil2() {
  }

  public String layout() {
    return "";
  }

  public Boolean fits(Integer w, Doc2 x) {
    // Not sure if this is the right approach.
    if (w < 0) {
      return false;
    }

    return true;
  }
}

class Text2 implements Doc2 {
  String s;
  Doc2 d;

  public Text2(String s, Doc2 d) {
    this.s = s;
    this.d = d;
  }

  public String layout() {
    return s + d.layout();
  }

  public Boolean fits(Integer w, Doc2 x) {
    if (w < 0) {
      return false;
    }

    return fits((w - s.length()), x);
  }
}

class Line2 implements Doc2 {
  Integer i;
  Doc2 d;

  public Line2(Integer i, Doc2 d) {
    this.i = i;
    this.d = d;
  }

  public String layout() {
    return '\n' + DOC1.copy(i, ' ') + d.layout();
  }

  public Boolean fits(Integer w, Doc2 x) {
    if (w < 0) {
      return false;
    }

    return true;
  }
}

class Nil1 implements DOC1 {
  public Nil1() {
  }

  public DOC1 flatten() {
    return new Nil1();
  }

  public Doc2 be(Integer w, Integer k, ArrayList<Pair<Integer, DOC1>> l) {
    if (l.isEmpty()) {
      return new Nil2();
    }

    try {
      // The first element in this list (this "Nil1") is to be ditched. So if after removing this one we get out of bounds then it means there's only one element and we can return Nil2.
      l.remove(0);
      return l.get(0).snd.be(w, k, l);
    } catch (IndexOutOfBoundsException e) {
      return new Nil2();
    }
  }
}

class CONCAT1 implements DOC1 {
  DOC1 left, right;

  public CONCAT1(DOC1 l, DOC1 r) {
    this.left = l;
    this.right = r;
  }

  public DOC1 flatten() {
    return new CONCAT1(left.flatten(), right.flatten());
  }

  public Doc2 be(Integer w, Integer k, ArrayList<Pair<Integer, DOC1>> l) {

    if (l.isEmpty()) {
      return new Nil2();
    }

    // I think we won't get out of bounds exception in this one because we'll break this CONCAT1 element into two element and put them back to the list.
    l.add(0, new Pair<Integer, DOC1>(l.get(0).fst, left));
    l.add(1, new Pair<Integer, DOC1>(l.get(0).fst, right));
    // However we'll have to remember to remove the original element
    l.remove(2);
    return left.be(w, k, l);
  }
}

class NEST1 implements DOC1 {
  Integer i;
  DOC1 d;

  public NEST1(Integer i, DOC1 d) {
    this.i = i;
    this.d = d;
  }

  public DOC1 flatten() {
    return new NEST1(i, d.flatten());
  }

  public Doc2 be(Integer w, Integer k, ArrayList<Pair<Integer, DOC1>> l) {
    if (l.isEmpty()) {
      return new Nil2();
    }

    // Seems we won't get out of bounds exception in this one because we'll always replace the first element in the list
    l.add(0, new Pair<Integer, DOC1>(i + l.get(0).fst, d));
    l.remove(1);

    return d.be(w, k, l);
  }
}

class TEXT1 implements DOC1 {
  String s;

  public TEXT1(String s) {
    this.s = s;
  }

  public DOC1 flatten() {
    // I think we should construct a new one instead of returning self.
    return new TEXT1(s);
  }

  public Doc2 be(Integer w, Integer k, ArrayList<Pair<Integer, DOC1>> l) {
    if (l.isEmpty()) {
      return new Nil2();
    }

    l.remove(0);

    try {
      return new Text2(s, l.get(0).snd.be(w, (k + s.length()), l));
    } catch (IndexOutOfBoundsException e) {
      // Means there was only one element in the list.
      return new Text2(s, new Nil2());
    }
  }
}

class LINE1 implements DOC1 {
  public LINE1() {
  }

  public DOC1 flatten() {
    return new TEXT1(" ");
  }

  public Doc2 be(Integer w, Integer k, ArrayList<Pair<Integer, DOC1>> l) {
    if (l.isEmpty()) {
      return new Nil2();
    }

    // The first element in this list (this "LINE1") is to be ditched. So if after removing this one we get out of bounds then it means there's only one element and we can return Nil2.
    try {
      return new Line2(l.get(0).fst, l.get(1).snd.be(w, l.get(0).fst, new ArrayList<Pair<Integer, DOC1>>(l.subList(1, l.size() - 1))));
    } catch (IndexOutOfBoundsException e) {
      // Means there was only one element in the list.
      return new Line2(l.get(0).fst, new Nil2());
    }
  }
}

class UNION1 implements DOC1 {
  DOC1 left, right;

  public UNION1(DOC1 l, DOC1 r) {
    this.left = l;
    this.right = r;
  }

  public DOC1 flatten() {
    return left.flatten();
  }

  public Doc2 be(Integer w, Integer k, ArrayList<Pair<Integer, DOC1>> l) {
    if (l.isEmpty()) {
      return new Nil2();
    }


    ArrayList<Pair<Integer, DOC1>> l1 = new ArrayList<Pair<Integer, DOC1>>(l);
    ArrayList<Pair<Integer, DOC1>> l2 = new ArrayList<Pair<Integer, DOC1>>(l);
    l1.remove(0);
    l1.add(0, new Pair<Integer, DOC1>(l.get(0).fst, left));
    l2.remove(0);
    l2.add(0, new Pair<Integer, DOC1>(l.get(0).fst, right));

    return Doc2.better(w, k, left.be(w, k, l1), right.be(w, k, l2));
  }
}



public class PrettyPrinterCombinators {
  public static void main(String[] args) {
    TEXT1 t = new TEXT1("aaa");
    System.out.println(DOC1.pretty(10, t));

    LINE1 li = new LINE1();
    System.out.println(DOC1.pretty(10, li));

    NEST1 n = new NEST1(4, t);
    System.out.println(DOC1.pretty(30, n));

    CONCAT1 c = new CONCAT1(t, li);
    System.out.println(DOC1.pretty(30, c));
    System.out.println(DOC1.pretty(30, new CONCAT1(t, n)));

    System.out.println(DOC1.pretty(30, new CONCAT1(new CONCAT1(new CONCAT1(new TEXT1("["), new NEST1(2, new CONCAT1(new LINE1(), new TEXT1("insideOfBracket")))), new LINE1()), new TEXT1("]"))));

    System.out.println(DOC1.pretty(30, new CONCAT1(new CONCAT1(new TEXT1("we're trying to print"), new LINE1()), new TEXT1("after a line"))));


    // Some bugs
//    Tree t11 = new Node ("ccc", new ArrayList<Tree>());
//    Tree t12 = new Node ("dd", new ArrayList<Tree>());
//    ArrayList<Tree> l1 = new ArrayList<Tree>();
//    l1.add(t11);
//    l1.add(t12);
//
//    Tree t1 = new Node ("bbbbb", l1);
//
//    Tree t2 = new Node ("eee", new ArrayList<Tree>());
//
//    Tree t31 = new Node ("gg", new ArrayList<Tree>());
//    Tree t32 = new Node ("hhh", new ArrayList<Tree>());
//    Tree t33 = new Node ("ii", new ArrayList<Tree>());
//    ArrayList<Tree> l3 = new ArrayList<Tree>();
//    l3.add(t31);
//    l3.add(t32);
//    l3.add(t33);
//
//    Tree t3 = new Node ("ffff", l3);
//
//    ArrayList<Tree> l = new ArrayList<Tree>();
//    l.add(t1);
//    l.add(t2);
//    l.add(t3);
//
//    Tree tr = new Node("aaa", l);
//
////    testTree(30, tr);
//
//    Tree tr2 = new Node("bbb", new ArrayList<Tree>());
//
//    testTree(30, tr2);

  }
}

// Some bugs with it. Not using it to test the code yet.
//interface Tree {
//  DOC1 showTree();
//
//  DOC1 showBracket(ArrayList<Tree> ts);
//
//  DOC1 showTrees(ArrayList<Tree> ts);
//}
//
//class Node implements Tree {
//  String s;
//  ArrayList<Tree> ts;
//
//  public Node(String s, ArrayList<Tree> ts) {
//    this.s = s;
//    this.ts = ts;
//  }
//
//  public DOC1 showTree() {
////      DOC1 d = new TEXT1(s);
//    return DOC1.group(new CONCAT1(new TEXT1(s), new NEST1(s.length(), showBracket(ts))));
//  }
//
//  public DOC1 showBracket(ArrayList<Tree> ts) {
//    if (ts.isEmpty()) {
//      return new Nil1();
//    }
//
//    return new CONCAT1(new CONCAT1(new TEXT1("["), new NEST1(1, showTrees(ts))), new TEXT1("]"));
//  }
//
//  public DOC1 showTrees(ArrayList<Tree> ts) {
//    if (ts.size() == 1) {
//      return ts.get(0).showTree();
//    }
//
//    return new CONCAT1(new CONCAT1(new CONCAT1(ts.get(0).showTree(), new TEXT1(",")), new LINE1()), showTrees(new ArrayList<Tree>(ts.subList(1, ts.size() - 1))));
//  }
//}
