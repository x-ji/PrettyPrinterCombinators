/**
 * Created by JX on 12.08.15.
 */
//package PrettyPrinterCombinators;

public interface DocAlg<Doc> {
  // Data types
  Doc Nil();
  Doc Text(String s, Doc d);
  Doc Line(Integer i, Doc d);
  Doc Union(Doc d1, Doc d2);
}

interface DPrinter<E> {
  E concat(E d2);
//  E nil();
//  E text(String s);
//  E line();
  E nest(Integer i, E d);
  String layout();

  E group();
  E flatten();
  E best(int w, int k);
  Boolean fits(int w);

//  String pretty(Integer i, E d);

//  static E nil() {

//  }
}

class DocPrinter implements DocAlg<DPrinter> {
  static DPrinter better(int w, int k, DPrinter d1, DPrinter d2) {
    if (d1.fits(w - k)) {
      return d1;
    } else {
      return d2;
    }
  }

  static String pretty(int w, DPrinter d) {
    DPrinter d2 = (DPrinter) d.best(w, 0);
    return d2.layout();
  }

  @Override
  public DPrinter Nil() {
    return new DPrinter<DPrinter>(){
      @Override
      public DPrinter concat(DPrinter d2) {
        return d2;
      }

      @Override
      public DPrinter nest(Integer i, DPrinter d) {
        return Nil();
      }

      @Override
      public String layout() {
        return "";
      }

      @Override
      public DPrinter group() {
        return Nil();
      }

      @Override
      public DPrinter flatten() {
        return Nil();
      }

      @Override
      public DPrinter best(int w, int k) {
        return Nil();
      }

      @Override
      public Boolean fits(int w) {
        return true;
      }

    };
  }

  @Override
  public DPrinter Text(String s, DPrinter d) {
    return new DPrinter<DPrinter>() {
      @Override
      public DPrinter concat(DPrinter d2) {
        return Text(s, (DPrinter) d.concat(d2));
      }

      @Override
      public DPrinter nest(Integer i, DPrinter d) {
        return Text(s, nest(i, d));
      }

      @Override
      public String layout() {
        return s + d.layout();
      }

      @Override
      public DPrinter group() {
        return Text(s, (DPrinter) d.group());
      }

      @Override
      public DPrinter flatten() {
        return Text(s, (DPrinter) d.flatten());
      }

      @Override
      public DPrinter best(int w, int k) {
        return Text(s, (DPrinter) d.best(w, (k + s.length())));
      }

      @Override
      public Boolean fits(int w) {
        return null;
      }
    };
  }

  @Override
  public DPrinter Line(Integer j, DPrinter d1) {
    return new DPrinter<DPrinter>() {
      @Override
      public DPrinter concat(DPrinter d2) {
        return Line(j, (DPrinter) d1.concat(d2));
      }

      @Override
      public DPrinter nest(Integer i, DPrinter d) {
        return Line(i + j, (DPrinter) d.nest(i, d));
      }

      @Override
      public String layout() {
        char[] temp = new char[j];
        java.util.Arrays.fill(temp, ' ');
        return "\n" + temp.toString() + d1.layout();
      }

      @Override
      public DPrinter group() {
        return Union(Text(" ", (DPrinter) d1.flatten()), Line(j, d1));
      }

      @Override
      public DPrinter flatten() {
        return Text(" ", (DPrinter) d1.flatten());
      }

      @Override
      public DPrinter best(int w, int k) {
        return Line(j, (DPrinter) d1.best(w, j));
      }

      @Override
      public Boolean fits(int w) {
        return true;
      }
    };
  }

  @Override
  public DPrinter Union(DPrinter d1, DPrinter d2) {
    return new DPrinter<DPrinter>() {
      @Override
      public DPrinter concat(DPrinter dr) {
        return Union(((DPrinter) d1.concat(dr)), (DPrinter) d2.concat(dr));
      }

      @Override
      public DPrinter nest(Integer i, DPrinter d) {
        return Union((DPrinter) d1.nest(i, d1), (DPrinter) d2.nest(i, d2));
      }

      @Override
      public String layout() {
        // Should just be the layout of one thing there?
        return d1.layout();
      }

      @Override
      public DPrinter group() {
        return Union((DPrinter) d1.group(), d2);
      }

      @Override
      public DPrinter flatten() {
        return (DPrinter) d1.flatten();
      }

      @Override
      public DPrinter best(int w, int k) {
        return better(w, k, (DPrinter) (d1.best(w, k)), (DPrinter) (d2.best (w, k)));
      }

      @Override
      public Boolean fits(int w) {
        return d1.fits(w);
      }
    };
  }
}

//interface DLayout {
//  String layout();
//}
//
//class DocLayout implements DocAlg<DLayout> {
//  @Override
//  public DLayout Nil() {
//    return new DLayout() {
//      public String layout() {
//        return "";
//      }
//    };
//  }
//
//  @Override
//  public DLayout Text(String s, DLayout d) {
//    return new DLayout() {
//      public String layout() {
//        return s + d.layout();
//      }
//    };
//  }
//
//  @Override
//  public DLayout Line(Integer i, DLayout d) {
//    return new DLayout() {
//      public String layout() {
//        char[] temp = new char[i];
//        java.util.Arrays.fill(temp, ' ');
//        return '\n' + temp.toString() + d.layout();
//      }
//    };
//  }
//
//  @Override
//  public DLayout Union(DLayout d1, DLayout d2) {
//    return new DLayout() {
//      public String layout() {
//        return d1.layout();
//      }
//    };
//  }
//}
//
//interface DNest {
//  String nest(int i);
//}




// A class represents an operation. This is the concrete implementation of an OA.
//class layout implements DocAlg<String> {
//
//  @Override
//  public String Nil() {
//    return "";
//  }
//
//  // Is this right? Directly returning d...
//  @Override
//  public String Text(String s, String d) {
//    return s + d;
//  }
//
//  @Override
//  public String Line(Integer i, String d) {
//    char[] temp = new char[i];
//    java.util.Arrays.fill(temp, ' ');
//    return "\n" + temp.toString() + d;
//  }
//
//  // What is this supposed to be
//  @Override
//  public String Union(String d1, String d2) {
//    return d1;
//  }
//}
//
//class group<Doc> implements DocAlg<Doc> {
//  DocAlg<Doc> alg;
//
//  group(DocAlg<Doc> alg) {this.alg = alg;}
//
//  @Override
//  public Doc Nil() {
//    return alg.Nil();
//  }
//
//  @Override
//  public Doc Text(String s, Doc d) {
//    return alg.Text(s, d);
//  }
//
//  @Override
//  public Doc Line(Integer i, Doc d) {
//    return alg.Union(alg.Text(" ", new flatten(alg)), alg.Line(i, d));
//  }
//
//  // What should this thing be.
//  @Override
//  public Doc Union(Doc d1, Doc d2) {
////    return alg.Union(new group<Doc>());
//  }
//}
//
//class best<Doc> implements DocAlg<Doc> {
//  int w;
//  int k;
//  DocAlg<Doc> alg;
//
//  best(int w, int k, DocAlg<Doc> alg) {this.w = w; this.k = k; this.alg = alg;}
//
//  @Override
//  public Doc Nil() {
//    return alg.Nil();
//  }
//
//  @Override
//  public Doc Text(String s) {
//    return alg.Text(s);
//  }
//
//  @Override
//  public Doc Line(Integer i) {
//    return null;
//  }
//
//  @Override
//  public Doc Union(Doc d1, Doc d2) {
//    return null;
//  }
//}
//
//class fits implements DocAlg<Boolean> {
//
//  int w;
//
//  fits(int w) {this.w = w;}
//
//  @Override
//  public Boolean Nil() {
//    if (w < 0) {
//      return false;
//    }
//
//    return true;
//  }
//
//  @Override
//  public Boolean Text(String s) {
//    if (w < 0) {
//      return false;
//    }
//
//    return new fits(w - s.length());
//  }
//
//  @Override
//  public Boolean Line(Integer i) {
//    return true;
//  }
//
//  @Override
//  public Boolean Union(Boolean d1, Boolean d2) {
//    return null;
//  }
//}
//
//class Nest<Doc> implements DocAlg<Doc> {
//
//  DocAlg<Doc> alg;
//
//  Nest(DocAlg<Doc> alg) { this.alg = alg; }
//
//  @Override
//  public Doc Nil() {
//    return
//  }
//
//  @Override
//  public Doc Text(String s) {
//    return alg.Text;
//  }
//
//  @Override
//  public Doc Line(Integer i) {
//    return null;
//  }
//
//  @Override
//  public Doc Union(Doc d1, Doc d2) {
//    return null;
//  }
//
//  @Override
//
//}
