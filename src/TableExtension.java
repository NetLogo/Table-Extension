package org.nlogo.extensions.table;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentIterator;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.AgentSetBuilder;
import org.nlogo.api.AnonymousReporter;
import org.nlogo.api.Argument;
import org.nlogo.api.Command;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Reporter;
import org.nlogo.core.CompilerException;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.nvm.ExtensionContext;
import org.nlogo.nvm.FileManager;

import java.util.Iterator;
import java.util.ArrayList;

import java.io.File;
import java.io.FileReader;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

// import org.json.JSONObject;
// import org.json.JSONTokener;
// import org.json.JSONParser;
// import com.typesafe.config.ConfigFactory;
// import com.typesafe.config.Config;


public class TableExtension
    extends org.nlogo.api.DefaultClassManager {

  public void load(org.nlogo.api.PrimitiveManager primManager) {
    primManager.addPrimitive("clear", new Clear());
    primManager.addPrimitive("get", new Get());
    primManager.addPrimitive("get-or-default", new GetOrDefault());
    primManager.addPrimitive("has-key?", new HasKey());
    primManager.addPrimitive("keys", new Keys());
    primManager.addPrimitive("length", new Length());
    primManager.addPrimitive("make", new Make());
    primManager.addPrimitive("put", new Put());
    primManager.addPrimitive("remove", new Remove());
    primManager.addPrimitive("from-list", new FromList());
    primManager.addPrimitive("counts", new Counts());
    primManager.addPrimitive("to-list", new ToList());
    primManager.addPrimitive("values", new Values());
    primManager.addPrimitive("group-items", new GroupItems());
    primManager.addPrimitive("group-agents", new GroupAgents());
    primManager.addPrimitive("from-json", new FromJSON());
  }

  private static java.util.WeakHashMap<Table, Long> tables = new java.util.WeakHashMap<Table, Long>();

  private static long next = 0;

  ///

  // It's important that we extend LinkedHashMap here, rather than
  // plain HashMap, because we want model results to be reproducible
  // crossplatform.

  public static class Table
      extends java.util.LinkedHashMap<Object, Object>
      // new NetLogo data types defined by extensions must implement
      // this interface
      implements org.nlogo.core.ExtensionObject {
    private final long id;

    public Table() {
      tables.put(this, next);
      id = next;
      next++;
    }

    public Table(LogoList alist)
        throws org.nlogo.api.ExtensionException {
      this();
      addAll(alist);
    }

    public void addAll(LogoList alist)
        throws ExtensionException {
      for (Iterator<Object> it = alist.javaIterator(); it.hasNext();) {
        Object pair = it.next();
        if ((pair instanceof LogoList
            && ((LogoList) pair).size() < 2)
            || (!(pair instanceof LogoList))) {
          throw new org.nlogo.api.ExtensionException
              ("expected a two-element list: " +
                  org.nlogo.api.Dump.logoObject(pair));
        }
        this.put(((LogoList) pair).first(),
            ((LogoList) pair).butFirst().first());
      }
    }

    public Table(long id) {
      this.id = id;
      tables.put(this, id);
      next = StrictMath.max(next, id + 1);
    }

    public Table(java.util.Map<Object,Object> map) {
      tables.put(this, next);
      id = next;
      next++;

      for (java.util.Map.Entry<Object,Object> entry : map.entrySet()) {
        this.put(entry.getKey(), getTableValue(entry.getValue()));
      }
    }

    private Object getTableValue(Object getValue) {
      // return the value to be added in a table being constructed from a Map
      if (getValue instanceof LinkedTreeMap) {
        return new Table((java.util.Map)getValue);
      } else if (getValue instanceof ArrayList) {
        LogoListBuilder alist = new LogoListBuilder();
        ((ArrayList)getValue).forEach((temp) -> {
          alist.add(getTableValue(temp));
        });
        return alist.toLogoList();
      } else {
        return getValue;
      }
    }

    public boolean equals(Object obj) {
      return this == obj;
    }

    public LogoList toList() {
      LogoListBuilder alist = new LogoListBuilder();
      for (Iterator<java.util.Map.Entry<Object, Object>> entries = entrySet().iterator(); entries.hasNext();) {
        java.util.Map.Entry<Object, Object> entry = entries.next();
        LogoListBuilder pair = new LogoListBuilder();
        pair.add(entry.getKey());
        pair.add(entry.getValue());
        alist.add(pair.toLogoList());
      }
      return alist.toLogoList();
    }

    public LogoList valuesList() {
      LogoListBuilder alist = new LogoListBuilder();
      for (Iterator<java.util.Map.Entry<Object, Object>> entries = entrySet().iterator(); entries.hasNext();) {
        java.util.Map.Entry<Object, Object> entry = entries.next();
        alist.add(entry.getValue());
      }
      return alist.toLogoList();
    }

    public String dump(boolean readable, boolean exportable, boolean reference) {
      if (exportable && reference) {
        return ("" + id);
      } else {
        return (exportable ? (id + ": ") : "") + org.nlogo.api.Dump.logoObject(this.toList(), true, exportable);
      }
    }

    public String getExtensionName() {
      return "table";
    }

    public String getNLTypeName() {
      // since this extension only defines one type, we don't
      // need to give it a name; "table:" is enough,
      // "table:table" would be redundant
      return "";
    }

    public boolean recursivelyEqual(Object o) {
      if (!(o instanceof Table)) {
        return false;
      }
      Table otherTable = (Table) o;
      if (size() != otherTable.size()) {
        return false;
      }
      for (Iterator<Object> iter = keySet().iterator(); iter.hasNext();) {
        Object key = iter.next();
        if (!otherTable.containsKey(key)
            || !org.nlogo.api.Equality.equals(get(key),
            otherTable.get(key))) {
          return false;
        }
      }
      return true;
    }
  }  // closing bracked for implements

  public void clearAll() {
    tables.clear();
    next = 0;
  }

  public StringBuilder exportWorld() {
    StringBuilder buffer = new StringBuilder();
    for (Table table : tables.keySet()) {
      buffer.append
          (org.nlogo.api.Dump.csv().encode
              (org.nlogo.api.Dump.extensionObject(table, true, true, false)) + "\n");
    }
    return buffer;
  }

  public void importWorld(java.util.List<String[]> lines, org.nlogo.api.ExtensionManager reader,
                          org.nlogo.api.ImportErrorHandler handler)
      throws ExtensionException {
    for (String[] line : lines) {
      try {
        reader.readFromString(line[0]);
      } catch (CompilerException e) {
        handler.showError("Error importing arrays", e.getMessage(), "This array will be ignored");
      }
    }
  }

  ///

  public static class Clear implements Command {
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax
          (new int[]{Syntax.WildcardType()});
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public void perform(Argument args[], Context context)
        throws ExtensionException, LogoException {
      Object arg0 = args[0].get();
      if (!(arg0 instanceof Table)) {
        throw new org.nlogo.api.ExtensionException
            ("not a table: " +
                org.nlogo.api.Dump.logoObject(arg0));
      }
      ((Table) arg0).clear();
    }
  }

  public static class Get implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax
          (new int[]{Syntax.WildcardType(), Syntax.WildcardType()},
              Syntax.WildcardType());
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      Object arg0 = args[0].get();
      if (!(arg0 instanceof Table)) {
        throw new org.nlogo.api.ExtensionException
            ("not a table: " +
                org.nlogo.api.Dump.logoObject(arg0));
      }
      Object key = args[1].get();
      Object result = ((Table) arg0).get(key);
      if (result == null) {
        throw new ExtensionException
            ("No value for " + org.nlogo.api.Dump.logoObject(key)
                + " in table.");
      }
      return result;
    }
  }

  public static class GetOrDefault implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(
              new int[] {Syntax.WildcardType(), Syntax.WildcardType(), Syntax.WildcardType()},
              Syntax.WildcardType()
      );
    }

    public Object report(Argument args[], Context context) throws ExtensionException {
      Object table = args[0].get();
      if (!(table instanceof Table)) {
        throw new org.nlogo.api.ExtensionException
                ("not a table: " +
                        org.nlogo.api.Dump.logoObject(table));
      }
      Object key = args[1].get();
      return ((Table) table).getOrDefault(key, args[2].get());
    }
  }

  public static class HasKey implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax
          (new int[]{Syntax.WildcardType(), Syntax.WildcardType()},
              Syntax.BooleanType());
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      Object arg0 = args[0].get();
      if (!(arg0 instanceof Table)) {
        throw new org.nlogo.api.ExtensionException
            ("not a table: " +
                org.nlogo.api.Dump.logoObject(arg0));
      }
      return Boolean.valueOf
          (((Table) arg0).containsKey(args[1].get()));
    }
  }

  public static class Keys implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax
          (new int[]{Syntax.WildcardType()},
              Syntax.ListType());
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      Object arg0 = args[0].get();
      if (!(arg0 instanceof Table)) {
        throw new org.nlogo.api.ExtensionException
            ("not a table: " +
                org.nlogo.api.Dump.logoObject(arg0));
      }
      return LogoList.fromJava(((Table) arg0).keySet());
    }
  }

  public static class Length implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax
          (new int[]{Syntax.WildcardType()},
              Syntax.NumberType());
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      Object arg0 = args[0].get();
      if (!(arg0 instanceof Table)) {
        throw new org.nlogo.api.ExtensionException
            ("not a table: " +
                org.nlogo.api.Dump.logoObject(arg0));
      }
      return Double.valueOf(((Table) arg0).size());
    }
  }

  public static class Make implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(Syntax.WildcardType());
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      return new Table();
    }

  }

  public static class FromJSON implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax
          (new int[]{Syntax.StringType()},
              Syntax.WildcardType());
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {

      FileManager fm = ((ExtensionContext) context).workspace().fileManager();

      try {
        String path = fm.attachPrefix(args[0].getString());
        File file = new File(path.toString());
        if (!file.exists()) {
          throw new ExtensionException (args[0].get().toString() + " does not exist.");
        }
        Gson gson = new Gson();
        java.util.Map<Object,Object> map = gson.fromJson(new FileReader(path), java.util.Map.class);
        return new Table(map);
      } catch (java.io.IOException e) {
        throw new ExtensionException(e.getMessage());
      }
    }
  }

  public static class Put implements Command {
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax
          (new int[]{Syntax.WildcardType(), Syntax.WildcardType(),
              Syntax.WildcardType()});
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public void perform(Argument args[], Context context)
        throws ExtensionException, LogoException {
      Object arg0 = args[0].get();
      if (!(arg0 instanceof Table)) {
        throw new org.nlogo.api.ExtensionException
            ("not a table: " +
                org.nlogo.api.Dump.logoObject(arg0));
      }
      Object key = args[1].get();
      ensureKeyValidity(key);
      ((Table) arg0).put(key, args[2].get());
    }
  }

  public static class Remove implements Command {
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax
          (new int[]{Syntax.WildcardType(), Syntax.WildcardType()});
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public void perform(Argument args[], Context context)
        throws ExtensionException, LogoException {
      Object arg0 = args[0].get();
      if (!(arg0 instanceof Table)) {
        throw new org.nlogo.api.ExtensionException
            ("not a table: " +
                org.nlogo.api.Dump.logoObject(arg0));
      }
      ((Table) arg0).remove(args[1].get());
    }
  }

  public static class ToList implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax
          (new int[]{Syntax.WildcardType()},
              Syntax.ListType());
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      Object arg0 = args[0].get();
      if (!(arg0 instanceof Table)) {
        throw new org.nlogo.api.ExtensionException
            ("not a table: " +
                org.nlogo.api.Dump.logoObject(arg0));
      }
      return ((Table) arg0).toList();

    }
  }

    public static class Values implements Reporter {
      public Syntax getSyntax() {
        return SyntaxJ.reporterSyntax
            (new int[]{Syntax.WildcardType()},
                  Syntax.ListType());
      }

      public String getAgentClassString() {
          return "OTPL";
      }

      public Object report(Argument args[], Context context)
          throws ExtensionException, LogoException {
        Object arg0 = args[0].get();
        if (!(arg0 instanceof Table)) {
          throw new org.nlogo.api.ExtensionException
                  ("not a table: " +
                          org.nlogo.api.Dump.logoObject(arg0));
        }
        return ((Table) arg0).valuesList();
      }
    }

  public static class FromList implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax
          (new int[]{Syntax.ListType()},
              Syntax.WildcardType());
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      LogoList alist = args[0].getList();
      return new Table(alist);
    }
  }

  public static class Counts implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(new int[]{Syntax.ListType()}, Syntax.WildcardType());
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public Object report(Argument args[], Context context) throws ExtensionException, LogoException {
      LogoList lst = args[0].getList();
      Table result = new Table();
      for (Object obj : lst.javaIterable()) {
        result.put(obj, 1.0 + (Double) result.getOrDefault(obj, 0.0));
      }
      return result;
    }
  }

  public static class GroupItems implements Reporter {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(
              new int[] {Syntax.ListType(), Syntax.ReporterType() },
              Syntax.WildcardType()
      );
    }

    @Override
    public Object report(Argument args[], Context context) throws ExtensionException, LogoException {
      LogoList lst = args[0].getList();
      AnonymousReporter classifier = args[1].getReporter();
      Table result = new Table();
      for (Object x : lst.toJava()) {
        Object group = classifier.report(context, new Object[] {x});
        ensureKeyValidity(group);
        result.put(group, ((LogoList) result.getOrDefault(group, LogoList.Empty())).lput(x));
      }
      return result;
    }
  }

  public static class GroupAgents implements Reporter {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(
              new int[] {Syntax.AgentsetType(), Syntax.ReporterBlockType()},
              Syntax.WildcardType(),
              "OTPL",
              "-TPL"
      );
    }

    @Override
    public Object report(Argument args[], Context context) throws ExtensionException, LogoException {
      AgentSet agents = (AgentSet) args[0].getAgentSet();
      org.nlogo.nvm.Reporter classifier = ((org.nlogo.nvm.Argument) args[1]).unevaluatedArgument();
      org.nlogo.nvm.Context childContext = new org.nlogo.nvm.Context(((ExtensionContext) context).nvmContext(), agents);
      AgentIterator agentIter = agents.shufflerator(context.getRNG());
      Table result = new Table();
      while (agentIter.hasNext()) {
        Agent agent = agentIter.next();
        Object group = childContext.evaluateReporter(agent, classifier);
        ensureKeyValidity(group);
        ((AgentSetBuilder) result.computeIfAbsent(group, k -> new AgentSetBuilder(agents.kind()))).add(agent);
      }
      result.replaceAll((k,v) -> ((AgentSetBuilder) v).build());
      return result;
    }
  }

  public org.nlogo.core.ExtensionObject readExtensionObject(org.nlogo.api.ExtensionManager reader,
                                                           String typeName, String value)
      throws org.nlogo.api.ExtensionException {
    try {
      String[] s = value.split(":");
      long id = Long.parseLong(s[0]);
      Table table = getOrCreateTableFromId(id);
      if (s.length > 1) {
        table.addAll((LogoList) reader.readFromString(s[1]));
      }
      return table;
    } catch (CompilerException ex) {
      throw new org.nlogo.api.ExtensionException(ex.getMessage());
    }
  }

  private Table getOrCreateTableFromId(long id) {
    for (Table table : tables.keySet()) {
      if (table.id == id) {
        return table;
      }
    }
    return new Table(id);
  }

  /// helpers

  private static boolean isValidKey(Object key) {
    return
        key instanceof Double ||
            key instanceof String ||
            key instanceof Boolean ||
            (key instanceof LogoList &&
                containsOnlyValidKeys((LogoList) key));
  }

  private static void ensureKeyValidity(Object key) throws ExtensionException {
    if (!isValidKey(key)) {
      throw new org.nlogo.api.ExtensionException
              (org.nlogo.api.Dump.logoObject(key) + " is not a valid table key "
                      + "(a table key may only be a number, a string, true or false, or a list "
                      + "whose items are valid keys)");

    }
  }

  private static boolean containsOnlyValidKeys(LogoList list) {
    for (Iterator<Object> it = list.javaIterator(); it.hasNext();) {
      Object o = it.next();
      if (!isValidKey(o)) {
        return false;
      }
    }
    return true;
  }


}
