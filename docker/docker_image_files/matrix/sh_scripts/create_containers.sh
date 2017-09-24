
index=1
while [ $index -le 6 ]
do
	echo "Create container $index"
	docker run -d -i -t -P --name java_t$index java/gig_node 
	(( index++ ))
done
