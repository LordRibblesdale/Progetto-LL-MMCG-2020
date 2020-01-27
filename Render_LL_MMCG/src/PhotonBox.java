//sistemare i commenti
public class PhotonBox {

	//vertici del box
    Point3D[] V=new Point3D[2];
    //array di fotoni
    Photon[] ph;
    int nph;

    //dimensione corrispondente alla normale del piano con cui viene suddiviso il box
    int  dim;
    //posizione del piano lungo la dimensione dim
    float planePos;

    public PhotonBox(Point3D v1, Point3D v2, Photon[] p, int n){
        V[0]=v1;
        V[1]=v2;

        ph=p;
        nph=n;
        if(n>0){
        Point3D max=new Point3D(Float.NEGATIVE_INFINITY);
        Point3D min=new Point3D(Float.POSITIVE_INFINITY);

        Point3D median=new Point3D();

        //assegnazione del parametro dim:
        //il parametro viene assegnato alla dimensione in cui i fotoni sono piu' distanti tra loro

        //calcolo il bounding box dei fotoni
        for(int i=0; i<nph; i++){

            Point3D pp= ph[i].position;

            //vedo se il vertice e' il massimo (se lo e' lo imposto come massimo)
            if(pp.x>max.x)
            	max.x= pp.x;
            if(pp.y>max.y)
            	max.y= pp.y;
            if(pp.z>max.z)
            	max.z= pp.z;
            //vedo se il vertice e' il minimo (se lo e' lo imposto come minimo)
            if(pp.x<min.x)
            	min.x= pp.x;
            if(pp.y<min.y)
            	min.y= pp.y;
            if(pp.z<min.z)
            	min.z= pp.z;
        }

        //ora viene scelto il parametro dim in base al lato del bounding box piu' lungo
        Point3D d= max.subtract(min);
        d.abs();
        float dist[]={d.x,d.y,d.z};

        //piano yz
        dim=0;
        if(d.y>dist[dim]){dim=1;}
        if(d.z>dist[dim]){dim=2;}

        //calcolo della mediana

        //ordinamento

        //si scorrono tutti gli elementi dell'array dei fotoni saltando il primo
        for(int i=1; i<nph; i++){
            //si salva il fotone in esame da una parte
            Photon pSaved=ph[i];

            //la posizione viene inserita in un array di 3 elementi
            float pos_i[]= {pSaved.position.x,pSaved.position.y,pSaved.position.z};

            //si controlla l'elemento precedente
            int j = i-1;

            //viene caricato l'elemento precedente in un array di 3 elementi
            float pos_j[]={ph[j].position.x,ph[j].position.y,ph[j].position.z};

            //se non e' stata controllata tutta la lista ordinata e l'elemento in posizione j e' piu' grande di quello in i allora i due fotoni vengono scambiati
            while ((j >= 0) && (pos_j[dim]>pos_i[dim])){

                //scambio
                ph[j + 1] = ph[j];

                //si scorre j
                j = j-1;

                //queste operazioni vengono effettuate solamente se la lista ordinata non e' stata scorsa completamente
                if(j>=0){
                pos_j[0]=ph[j].position.x;
                pos_j[1]=ph[j].position.y;
                pos_j[2]=ph[j].position.z;}
                //l'elemento che prima era in posizione j ora e' p
                ph[j+1]=pSaved;
                }
        }

        //il fotone che ci interessa e' quello a meta' di questo array
        int mpos= (int) Math.floor(nph/2);
        System.out.println("mpos"+mpos);

        //la mediana e' costituita proprio dalla posizione di questo fotone
        median.copy(ph[mpos].position);
        System.out.println("median x: "+median.x+" y: "+median.y+" z: "+median.z);
        //viene quindi assegnata la posizione del piano in base al parametro dim
        float m[]={median.x,median.y,median.z};
        planePos=m[dim];
        System.out.println("planePos"+planePos);

        }
    }
}