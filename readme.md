
# Cut
 a command to extract data from the file.

 `-f` option to specify the range(s) of fields/specific field(s) to extract.

 `-b` option to specify the range(s) of bytes/specific byte(s) to extract.

 `-c` option to specify the range(s) of characters/specific character(s) to extract.

 `-d` option to specify the character based on which data is separated into fields.

 `--output-delimiter` join the extracted fields with the given delimiter.

 `--complement` inverses the criteria to field/bytes/character selection.

 `--s` or `--only-delimited` to only print lines which contains delimiter.

 Checkout testcase under `cut` directory for getting some examples for understanding this command better.

 Note: 
  1. range can be open-ended from any one side (start or end), but not from both side.
  2. `-f`, `-b`, `-c` are mutually exclusive options.
  3. for `-b` & `-c` option implementation is same, due to which, this implementation be able to handle unicode characters as expected. 

# Sort
 a command to sort data from file(s)
 
 `-r` or `--reverse` option to reverse the output of the sort.

 `-u` or `--unique` option to output

 `-b` or `--ignore-case` option to ignore leading blank spaces & perform the sort.

 `-f` or `--dictionary-order` option to sort input based on alphanumeric & whitespace only.
 
 `-g` or `--general-numeric-sort` option to sort numbers (supports exponential numbers as well).
 
 `-M` or `--month-sort` option to sort months (short form or even prefixes of the months).

 `-n` or `--numeric-sort` option to sort numbers (doesn't support sorting of exponential numbers).
 
 `-h` or `--human-numeric-sort` option to sort numbers based on numeric units (such as, Byte(B), Kilo(K) or Kilo-Byte(KB), Giga(G) or Giga-Byte(GB), Tera(T) or Tera-Byte(TB), Peta(P) or Peta-Byte(PB), Exa(E) or Exa-Byte(EB), Zetta-Byte(ZB), Yotta-Byte(YB))
 
 `-V` or `--version-sort` option to sort versions provided
 
 `-R` or `--random-sort` option to sort entries randomly

 Note: 
 
 1. Multiple file paths can be specified to sort the entries as a single unit.
 2. `-f`, `-g`, `-M`, `-n`, `-h`, `-V`, `-R` are mutually exclusive options.
 3. Testcases for Sort are due. 

