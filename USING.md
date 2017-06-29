## Using

The table extension is pre-installed in NetLogo.

To use the table extension in your model, add a line to the top of your Code tab:

```
extensions [table]
```

If your model already uses other extensions, then it already has an
`extensions` line in it, so just add `table` to the list.

For more information on using NetLogo extensions,
see the [Extensions Guide](http://ccl.northwestern.edu/netlogo/docs/extensions.html)

### When to Use

In general, anything you can do with an table in NetLogo, you could
also just use a list for. But you may want to consider using an table
instead for speed reasons. Lists and tables have different performance
 characteristics, so you may be able to make your model run faster by
selecting the appropriate data structure.

Tables are useful when you need to do associate values with other
values. For example, you might make a table of words and their
definitions. Then you can look up the definition of any word. Here,
the words are the &quot;keys&quot;. You can easily retrieve the value
for any key in the table, but not vice versa.

Unlike NetLogo's lists and strings, tables are
"mutable". That means that you can actually modify them
directly, rather than constructing an altered copy as with lists. If
the table is used in more than one place in your code, any
changes you make will show up everywhere. It's tricky to write
code involving mutable structures and it's easy to make subtle
errors or get surprising results, so we suggest sticking with lists
and strings unless you're certain you want and need mutability.

### Example

{{! don't turn extension object into template variable }}{{= | | =}}
```NetLogo
let dict table:make
table:put dict "turtle" "cute"
table:put dict "bunny" "cutest"
print dict
=> {{table: "turtle" -> "cute", "bunny" -> "cutest" }}
print table:length dict
=> 2
print table:get dict "turtle"
=> "cute"
print table:get dict "leopard"
=> (error)
print table:keys dict
=> ["turtle" "bunny"]
```
