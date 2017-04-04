BEGIN{
    while(getline < zoneMapFile == 1){

	if($1==1){a1[$2]=$3;}
        else if($1==35){a35[$2]=$3;}
        else if($1==36){a36[$2]=$3;}
        else if($1==38){a38[$2]=$3;}
    }
}
{
    if(a1[$3]!="" && (a1[$4]!="" || $4==0 )){
        if($4==0){
            sec=0;
        }else{
            sec=a1[$4];
        }

        print "1\t"$1"\t"$2"\t"a1[$3]"\t"sec"\t";

    }else{
        if($3!=37){
           error["("$3"-"$4")"]==1;
        }
    }

        if($5==5){
            if(a36[$3]!=""){
                print "2\t"$1"\t"$2"\t36\t"a36[$3]"\t";
            }else{
                error["<36,"$3">"]=1
            }
        } else if($5==6){
            if(a38[$3]!=""){
                print "4\t"$1"\t"$2"\t38\t"a38[$3]"\t";
            }else{
                error["<38,"$3">"]=1
            }
        } else if($5==2 || $5==32){
            if(a35[$3]!=""){
                print "2\t"$1"\t"$2"\t35\t"a35[$3]"\t";
            }else{
                error["<35,"$3">"]=1
            }
        }
}
END{
    close(zoneMapFile);
    for(x in error)print x > "data/zoneNotFound.txt";
}
