package com.yusufkutluay.haydigezeknavigation.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yusufkutluay.haydigezeknavigation.Model.BilgiModel
import com.yusufkutluay.haydigezeknavigation.Model.Place
import com.yusufkutluay.haydigezeknavigation.Model.RotaListModel
import com.yusufkutluay.haydigezeknavigation.Model.SehirModel
import com.yusufkutluay.haydigezeknavigation.Model.UsersModel


class FirestoreDatabase {

    val db = Firebase.firestore
    var toastMessage : Boolean = false

    fun signUp(userName : String, user : String,email : Any, password : Any){

        // burda user kullanıcı için gerekli parametreler belirlendi ve firebase e aktarıldı
        val signUpHashMap = hashMapOf(
            "name" to userName,
            "user" to user,
            "email" to email,
            "password" to password
        )

        db.collection("users")
            .add(signUpHashMap)
            .addOnSuccessListener {
                toastMessage = true
            }
            .addOnFailureListener {
                toastMessage = false
            }

    }

       // burda fireabaseden verileri eklemek yerine burdan yolladım
        fun addFirestore(){

            val placeHashMap = hashMapOf(
                "enlem" to "41.00872060491868",
                "boylam" to "28.980668522949102",
                "url" to "https://firebasestorage.googleapis.com/v0/b/haydi-gezek-end.appspot.com/o/Istanbul%2Fayasofya.png?alt=media&token=122251f9-39d5-4d04-91b2-10e121aea133",
                "name" to "Ayasofya Camii",
                "aciklama" to "İstanbul'un Kalbi: Ayasofya\n" +
                        "\n"+
                        "İstanbul'un tarihi yarımadasında yer alan Ayasofya, görkemli mimarisi ve zengin geçmişiyle her yıl milyonlarca ziyaretçiyi ağırlıyor. 537 yılında Bizans İmparatoru I. Justinianus tarafından kilise olarak inşa edilen bu yapı, dönemin en büyük ve en etkileyici dini yapılarından biriydi.\n" +
                        "\n" +
                        "Ayasofya'nın iç mekânına adım attığınızda, devasa kubbesi ve zarif mozaikleri sizi büyüler. Altın yaldızlı mozaiklerde İsa, Meryem Ana ve çeşitli azizlerin tasvirlerini görürsünüz. Bizans dönemine ait bu mozaikler, dini ve sanatsal açıdan büyük bir öneme sahiptir.\n" +
                        "\n" +
                        "1453 yılında İstanbul'un fethiyle birlikte Ayasofya, Fatih Sultan Mehmet tarafından camiye dönüştürülmüştür. Bu dönemde yapıya minareler eklenmiş ve iç mekânda İslam sanatının zarif örnekleri olan hat yazıları yer almıştır. 1935 yılında Türkiye Cumhuriyeti'nin kurucusu Mustafa Kemal Atatürk'ün kararıyla müze haline getirilmiş, 2020 yılında ise tekrar cami olarak kullanılmaya başlanmıştır.\n" +
                        "\n" +
                        "Ayasofya'nın galerilerine çıktığınızda, İstanbul'un eşsiz manzarası sizi karşılar. Bu galeriler, hem iç mekânın güzelliklerini hem de Boğaz'ın muhteşem manzarasını sunar. Ayasofya'nın avlusunda yer alan şadırvan ve medrese kalıntıları, yapının zengin tarihine dair ipuçları verir.\n" +
                        "\n" +
                        "Ayasofya, İstanbul'un kültürel mozaiğinin bir sembolü olarak, farklı medeniyetlerin izlerini taşır. Hem Bizans hem de Osmanlı dönemlerinin en önemli eserlerinden biri olan bu yapı, tarihin derinliklerine doğru bir yolculuğa çıkarır ziyaretçilerini.\n" +
                        "\n" +
                        "İstanbul'u ziyaret ettiğinizde, Ayasofya'yı görmeden dönmeyin. Bu muazzam yapı, hem mimari hem de kültürel açıdan size unutulmaz anılar ve derin bir tarih bilinci kazandıracaktır."
            )
            val documentId = "ayasofya"

            db.collection("Sehirler")
                .document("istanbul")
                .collection("GezilecekYerler")
                .document(documentId)
                .set(placeHashMap)

        }

    fun aciklamaEkle(){

        db.collection("Sehirler")
            .document("diyarbakir")
            .collection("GezilecekYerler")
            .document("zulkufDag")
            .update("aciklama" , "Merhaba")
    }

    fun sehir(listeyiDondur: (ArrayList<SehirModel>) -> Unit){

        db
            .collection("Sehirler")
            .get()
            .addOnSuccessListener {

                val sehirList = ArrayList<SehirModel>()

                for (document in it){

                    val sehirName = document.getString("name")
                    val sehirGorsel = document.getString("url")

                    if (sehirName != null && sehirGorsel != null){
                        val sehir = SehirModel(sehirName,sehirGorsel)
                        sehirList.add(sehir)
                    }

                }

                sehirList.sortBy { it.name }

                listeyiDondur(sehirList)

            }

    }


    // firebase verileri okuma
    fun getVeri(sehir : String, listeyiDondur: (ArrayList<Place>) -> Unit){

        db.collection("Sehirler")
            .document(sehir)
            .collection("GezilecekYerler")
            .get()
            .addOnSuccessListener {
                val postList = ArrayList<Place>()
                for (document in it){

                    val enlem = document.getString("enlem")?.toDouble()
                    val boylam = document.getString("boylam")?.toDouble()
                    val urlGorsel = document.getString("url")
                    val name = document.getString("name")

                    if (enlem != null && boylam != null && urlGorsel != null && name != null) {
                        val place = Place(enlem, boylam, urlGorsel, name)
                        postList.add(place)
                    }else{
                       println("null")

                    }

                }

                postList.sortBy { it.name }

                listeyiDondur(postList)


            }

    }

    private fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.email
    }

    fun getRota(listeyiDondur: (ArrayList<RotaListModel>) -> Unit){

        getCurrentUserId()?.let {
            db.collection("usersRota")
                .document(it)
                .collection("selectedPlaces")
                .get()
                .addOnSuccessListener {
                    val rotaList = ArrayList<RotaListModel>()

                    for (document in it){

                        val documentId = document.id

                        val enlem = document.getString("enlem")?.toDouble()
                        val boylam = document.getString("boylam")?.toDouble()
                        val urlGorsel = document.getString("url")
                        val name = document.getString("name")
                        val mesafe = document.getString("mesafe")

                        if (enlem != null && boylam != null && urlGorsel != null && name != null) {
                            val place = RotaListModel(enlem, boylam, urlGorsel, name,documentId)
                            rotaList.add(place)
                        }else{
                            println("null")

                        }
                    }
                    rotaList.sortBy { it.nameRota }
                    listeyiDondur(rotaList)
                }
        }

    }

    fun getUsers(listeyiDondur: (ArrayList<UsersModel>) -> Unit){

        db.collection("users")
            .get()
            .addOnSuccessListener {
                val usersList = ArrayList<UsersModel>()
                for (document in it){

                    val email = document.getString("email")!!
                    val namePerson = document.getString("name")!!

                    if (email.isNotEmpty() || namePerson.isNotEmpty()){
                        val listPerson = UsersModel(email,namePerson)
                        usersList.add(listPerson)
                    }

                    listeyiDondur(usersList)


                }
            }
    }


    // Firestore'dan bilgi al
    fun getBilgi(sehir: String?, nameBilgi: String?, listeyiDondur: (ArrayList<BilgiModel>) -> Unit) {
        if (nameBilgi == null && sehir == null) {
            // Hata durumu, isim bilgisi null
            return
        }

        db.collection("Sehirler")
            .document(sehir!!)
            .collection("GezilecekYerler")
            .whereEqualTo("name", nameBilgi)
            .get()
            .addOnSuccessListener { documents ->
                val bilgiList = ArrayList<BilgiModel>()

                for (document in documents) {

                        val url = document.getString("url") ?: ""
                        val aciklama = document.getString("aciklama") ?: ""

                        if (url.isNotEmpty() && aciklama.isNotEmpty()) {
                            val bilgi = BilgiModel(nameBilgi!!, url, aciklama)
                            bilgiList.add(bilgi)
                        } else {
                            println("Null değerler var")
                        }

                }
                listeyiDondur(bilgiList)
            }
    }
}