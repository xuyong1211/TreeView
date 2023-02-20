package com.xuyong.treeview

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xuyong.treeview.group.TouchableTreeView
import com.xuyong.treeview.group.TreeAdapterImp
import com.xuyong.treeview.group.TreeNode
import com.xuyong.treeview.group.TreeView
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //  造一组树形结构的数据
        val treeView: TouchableTreeView = findViewById<View>(R.id.treeView) as TouchableTreeView
        val adapterImp = TreeAdapterImp()
        val treeNode1 = TreeNode("侄子")
        val treeNode2 = TreeNode("侄女")
        val list1: ArrayList<TreeNode> = ArrayList<TreeNode>()
        list1.add(treeNode1)
        list1.add(treeNode2)
        val treeNode3 = TreeNode("外甥")
        val treeNode4 = TreeNode("外甥女")
        val list2: ArrayList<TreeNode> = ArrayList<TreeNode>()
        list2.add(treeNode3)
        list2.add(treeNode4)
        val treeNode5 = TreeNode("哥哥")
        val treeNode6 = TreeNode("姐姐")
        treeNode5.children = (list1)
        treeNode6.children = (list2)
        val treeNode7 = TreeNode("曾孙")
        val treeNode8 = TreeNode("曾孙女")
        val list3: ArrayList<TreeNode> = ArrayList<TreeNode>()
        list3.add(treeNode7)
        list3.add(treeNode8)
        val treeNode9 = TreeNode("外曾孙")
        val treeNode10 = TreeNode("外曾孙女")
        val list4: ArrayList<TreeNode> = ArrayList<TreeNode>()
        list4.add(treeNode9)
        list4.add(treeNode10)
        val treeNode11 = TreeNode("孙子")
        val treeNode12 = TreeNode("孙女")
        treeNode11.children = (list3)
        treeNode12.children = (list4)
        val list5: ArrayList<TreeNode> = ArrayList<TreeNode>()
        list5.add(treeNode11)
        list5.add(treeNode12)
        val treeNode13 = TreeNode("外孙")
        val treeNode14 = TreeNode("外孙女")
        val list6: ArrayList<TreeNode> = ArrayList<TreeNode>()
        list6.add(treeNode13)
        list6.add(treeNode14)
        val treeNode15 = TreeNode("儿子")
        val treeNode16 = TreeNode("女儿")
        treeNode15.children = (list5)
        treeNode16.children = (list6)
        val list7: ArrayList<TreeNode> = ArrayList<TreeNode>()
        list7.add(treeNode15)
        list7.add(treeNode16)
        val treeNode17 = TreeNode("自己")
        treeNode17.children = (list7)
        val treeNode18 = TreeNode("侄子")
        val treeNode19 = TreeNode("侄女")
        val list8: ArrayList<TreeNode> = ArrayList<TreeNode>()
        list8.add(treeNode18)
        list8.add(treeNode19)
        val treeNode20 = TreeNode("弟弟")
        treeNode20.children = (list8)
        val treeNode21 = TreeNode("外甥")
        val treeNode22 = TreeNode("外甥女")
        val list9: ArrayList<TreeNode> = ArrayList<TreeNode>()
        list9.add(treeNode21)
        list9.add(treeNode22)
        val treeNode23 = TreeNode("妹妹")
        treeNode23.children = (list9)
        val list10: ArrayList<TreeNode> = ArrayList<TreeNode>()
        list10.add(treeNode5)
        list10.add(treeNode6)
        list10.add(treeNode17)
        list10.add(treeNode20)
        list10.add(treeNode23)
        val treeNode24 = TreeNode("父亲")
        treeNode24.children = list10


        adapterImp.treeNode = treeNode24
        adapterImp.nodeClickListener = { node->
            Toast.makeText(this@MainActivity,node.name1,Toast.LENGTH_SHORT).show()
        }
        treeView.adapter = (adapterImp)
    }
}