
index=1
while [ $index -le 6 ]
do
	echo "Kill container $index"
	docker rm -f java_t$index
	(( index++ ))
done
