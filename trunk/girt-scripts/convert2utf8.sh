
for file in *.xml; 
do 
iconv --from-code=iso-8859-1 --to-code=utf-8 $file > $file; 
done
 
for file in *.utf8;
do
cp $file `basename $file .utf8`;
done
