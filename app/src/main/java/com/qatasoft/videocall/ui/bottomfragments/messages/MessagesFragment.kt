package com.qatasoft.videocall.ui.bottomfragments.messages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.*
import com.qatasoft.videocall.MainActivity
import com.qatasoft.videocall.R
import com.qatasoft.videocall.ui.chatmessage.ChatLogActivity
import com.qatasoft.videocall.data.db.entities.ChatMessage
import com.qatasoft.videocall.data.db.entities.User
import com.qatasoft.videocall.registerlogin.LoginActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import com.qatasoft.videocall.data.db.entities.Tools
import com.qatasoft.videocall.request.FBaseControl
import com.qatasoft.videocall.ui.bottomfragments.home.HomeFragment
import com.qatasoft.videocall.ui.chatmessage.ChatMessageViewModel
import com.qatasoft.videocall.ui.chatmessage.ChatMessageViewModelFactory
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.fragment_messages.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.android.kodein
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class MessagesFragment : Fragment(), SearchView.OnQueryTextListener, KodeinAware {

    companion object {
        const val logTAG = "MessagesFragment"
        const val USER_KEY = "USER_INFO_KEY"
    }

    override val kodein by closestKodein()
    private val factory: ChatMessageViewModelFactory by instance()

    private var searchText = ""
    var tabIndex = 0
    var users = ArrayList<String>()

    private val adapter = GroupAdapter<ViewHolder>()
    private lateinit var viewModel: ChatMessageViewModel
    private val mUser = MainActivity.mUser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getInfo()

        adapter.setOnItemClickListener { item, _ ->
            val row = item as MessageItem

            val intent = Intent(activity, ChatLogActivity::class.java)
            intent.putExtra(USER_KEY, row.user)
            startActivity(intent)
        }

        controlSession()

        messages_searchview.setOnQueryTextListener(this)

        tabOnSelect()
    }

    private fun controlSession() {
        //Kullanıcı Giriş Yapmamış ise onu LoginActivity'e geri atar. Ve Geri dönemez.
        if (mUser.uid.isEmpty()) {
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }


    private fun getInfo() {
        viewModel = ViewModelProvider(this, factory).get(ChatMessageViewModel::class.java)
        
        recycler_message_user.adapter = adapter
        //Itemlar arasında ayıraç konuluyor
        recycler_message_user.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))

        if (HomeFragment.isMessage) {
            tabLayout.getTabAt(0)?.select()
            fetchInfo(Tools.messageType)
        } else {
            tabLayout.getTabAt(1)?.select()
            fetchInfo(Tools.callType)
        }
    }

    private fun fetchInfos(type: String) {
        adapter.clear()
        users.clear()

        val ref = FirebaseDatabase.getInstance().getReference("/$type/${mUser.uid}")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val data = p0.getValue(ChatMessage::class.java) ?: return

                val chatPartnerId = if (data.fromId == mUser.uid) {
                    data.toId
                } else {
                    data.fromId
                }

                if (users.indexOf(chatPartnerId) < 0) {
                    users.add(chatPartnerId)
                    fetchUserInfo(data, chatPartnerId)
                }
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val data = p0.getValue(ChatMessage::class.java) ?: return

                val chatPartnerId = if (data.fromId == mUser.uid) {
                    data.toId
                } else {
                    data.fromId
                }

                if (users.indexOf(chatPartnerId) < 0) {
                    users.add(chatPartnerId)
                    fetchUserInfo(data, chatPartnerId)
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    private fun fetchInfo(type: String) {
        adapter.clear()
        users.clear()

        val ref = FirebaseDatabase.getInstance().getReference("/$type/${mUser.uid}")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val data = p0.getValue(ChatMessage::class.java) ?: return

                Log.d(logTAG, "Deneme :" + data.toId)

                val chatPartnerId = if (data.fromId == mUser.uid) {
                    data.toId
                } else {
                    data.fromId
                }

                if (users.indexOf(chatPartnerId) < 0) {
                    users.add(chatPartnerId)

                    fetchUserInfo(data, chatPartnerId)
                }
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val data = p0.getValue(ChatMessage::class.java) ?: return

                val chatPartnerId = if (data.fromId == mUser.uid) {
                    data.toId
                } else {
                    data.fromId
                }

                if (users.indexOf(chatPartnerId) < 0) {
                    users.add(chatPartnerId)
                    fetchUserInfo(data, chatPartnerId)
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    fun fetchUserInfo(data: ChatMessage, chatPartnerId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                val user = p0.getValue(User::class.java)

                if (user != null) {
                    Log.d(logTAG, "Users Size : ${users.size} ${user.username}")

                    if (user.username.contains(searchText) && users.indexOf(chatPartnerId) >= 0) {
                        Log.d(logTAG, user.username)
                        users.remove(chatPartnerId)
                        adapter.add(MessageItem(data, user, context!!))
                    }
                } else {
                    Log.d(logTAG, "Problem Users Size : ${users.size}")
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(logTAG, "There is a problem while fetching User Info : ${p0.message}")
            }
        })
    }

    //Serchs user from firebase with text which user input
    private fun searchUserFromFirebase(newText: String?): Boolean {
        if (newText != null) {
            users.clear()
            if (tabIndex == 0) {
                adapter.clear()
                searchText = newText.toString()
                fetchInfo(Tools.messageType)
            } else if (tabIndex == 1) {
                adapter.clear()
                searchText = newText.toString()
                fetchInfo(Tools.callType)
            }
            return true
        }
        return false
    }

    private fun tabOnSelect() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {

                Log.d(logTAG, tab?.position.toString())
                adapter.clear()
                when (tab?.position) {
                    0 -> {
                        tabIndex = 0
                        fetchInfo(Tools.messageType)
                    }

                    1 -> {
                        tabIndex = 1
                        fetchInfo(Tools.callType)
                    }
                }
            }
        })
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return searchUserFromFirebase(query)
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return searchUserFromFirebase(newText)
    }
}