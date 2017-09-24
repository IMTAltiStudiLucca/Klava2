
index=1
while [ $index -le 100 ]
do
	echo "Kill container $index"
	docker restart python_t$index
	(( index++ ))
done
