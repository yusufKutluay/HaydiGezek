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
                "enlem" to "40.6166028",
                "boylam" to "40.2960562",
                "url" to "https://firebasestorage.googleapis.com/v0/b/haydi-gezek-end.appspot.com/o/Trabzon%2Fuzungol.png?alt=media&token=5f428edd-dc5c-431a-b580-9dbcea3b1bc3",
                "name" to "Uzungöl",
                "aciklama" to "Trabzon’a 99 kilometre, Çaykara ilçesine ise sadece 19 kilometre uzaklıkta yer alan Uzungöl, Trabzon’un en sevilen ve en çok ziyaret edilen turistik noktalarından biri. Her yıl yüz binlerce turist tarafından ziyaret edilen Uzungöl, Haldizen Deresi’nin doğal yollarla önünün kapanması yoluyla oluşmuş. Deniz seviyesinden 1100 metre yüksekte yer alan ve çevresi çam ormanları ile görkemli dağlar tarafından çevrilenmiş Uzungöl, aynı zamanda Türkiye’nin yağmur ormanlarına da ev sahipliği yapıyor.\n" +
                        "\n" +
                        "2004 yılında çevre koruma alanı ilan edilerek doğal güzellikleri korunan Uzungöl; 60'dan fazla endemik bitki türü, 250'den fazla kuş türü ve onlarca memeli türü ile tüm dünyadan doğa severlerin ilgisini çekiyor. Her mevsim muhteşem doğa manzaraları için ziyaret edilen Uzungöl’de yaz aylarında kano, deniz bisikleti ve sandallarla gezebilir, gölün çevresini saran yürüyüş yollarında tertemiz havada keyifli doğa yürüyüşleri yapabilir; ATV turları ve bisiklet gezileri ile Uzungöl çevresini keşfe çıkabilirsiniz. Göl üzerinde kuş bakışı büyüleyici manzaralar izlemek için yamaç paraşütü etkinliklerine de katılabilir veya yaz aylarında yayla şenlikleri ile eğlenceli zaman geçirebilirsiniz."
            )
            val documentId = "uzungol"

            db.collection("Sehirler")
                .document("trabzon")
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