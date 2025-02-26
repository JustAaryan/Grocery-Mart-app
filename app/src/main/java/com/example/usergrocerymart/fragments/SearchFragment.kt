package com.example.usergrocerymart.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.usergrocerymart.CartListener
import com.example.usergrocerymart.R
import com.example.usergrocerymart.adapters.AdapterProduct
import com.example.usergrocerymart.databinding.FragmentSearchBinding
import com.example.usergrocerymart.databinding.ItemViewprodBinding
import com.example.usergrocerymart.mesg
import com.example.usergrocerymart.models.Product
import com.example.usergrocerymart.roomdb.CartProducts
import com.example.usergrocerymart.viewmodels.UserViewModel
import kotlinx.coroutines.launch
import java.lang.ClassCastException


class SearchFragment : Fragment() {
    val viewModel: UserViewModel by viewModels()
private lateinit var binding: FragmentSearchBinding
private lateinit var adapterProduct: AdapterProduct
    private var cartListener : CartListener?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(layoutInflater)
        getallrpoducts()
        searchproducts()
        backtohome()
        return binding.root
    }
    private fun searchproducts() {
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                adapterProduct.filter?.filter(query)

            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }
    private fun backtohome() {
        binding.searchEt.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_HomeFragment)}
        }


    private fun getallrpoducts() {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        lifecycleScope.launch {
            viewModel.fetchallprod().collect {
                if (it.isEmpty()) {
                    binding.rvproducts.visibility = View.GONE
                    binding.tvtext.visibility = View.VISIBLE
                } else {
                    binding.rvproducts.visibility = View.VISIBLE
                    binding.tvtext.visibility = View.GONE
                }
                adapterProduct = AdapterProduct(
                    ::onaddbtnclick,
                    ::onincrementbtnclick,
                    ::ondecreamentbtnclick
                )
                binding.rvproducts.adapter = adapterProduct
                adapterProduct.differ.submitList(it)
                adapterProduct.originallist = it as ArrayList<Product>
                binding.shimmerViewContainer.visibility = View.GONE
            }
        }
    }
    private  fun onincrementbtnclick(product: Product,productBinding: ItemViewprodBinding){

        var itemcntinc = productBinding.prodcnt.text.toString().toInt()
        itemcntinc++
        if(product.Prodstock!! + 1 > itemcntinc) {
            productBinding.prodcnt.text = itemcntinc.toString()
            cartListener?.showcartlayout(1)
            product.itemcount = itemcntinc
            lifecycleScope.launch {
                cartListener?.savingCartItem(1)
                saveprodinroomdb(product)
                viewModel.updateitemcnt(product, itemcntinc)
            }
        }
        else{
            mesg.showtoast(requireContext(),"Out Of Stock")
        }
    }
    private fun ondecreamentbtnclick(product: Product,productBinding: ItemViewprodBinding){

        var itemcntdec = productBinding.prodcnt.text.toString().toInt()
        itemcntdec--
        product.itemcount = itemcntdec
        lifecycleScope.launch {
            cartListener?.savingCartItem(-1)
            saveprodinroomdb(product)
            viewModel.updateitemcnt(product,itemcntdec)
        }
        if(itemcntdec > 0){
            productBinding.prodcnt.text = itemcntdec.toString()
        }
        else{
            lifecycleScope.launch {
                viewModel.deletecartprod(product.prod_id!!)
            }
            productBinding.tvedit.visibility = View.VISIBLE
            productBinding.llprodcnt.visibility = View.GONE
            productBinding.prodcnt.text = "0"

        }
        cartListener?.showcartlayout(-1)

    }
    private fun onaddbtnclick(product: Product,productBinding: ItemViewprodBinding){
        productBinding.tvedit.visibility = View.GONE
        productBinding.llprodcnt.visibility = View.VISIBLE
        var itemcnt = productBinding.prodcnt.text.toString().toInt()
        itemcnt++
        productBinding.prodcnt.text = itemcnt.toString()
        cartListener?.showcartlayout(1)
        product.itemcount = itemcnt
        lifecycleScope.launch {
            cartListener?.savingCartItem(1)
            saveprodinroomdb(product)
            viewModel.updateitemcnt(product,itemcnt)
        }

    }
    private fun saveprodinroomdb(product: Product) {

        val cartproduct = CartProducts(
            Prod_id = product.prod_id!!,
            Prodtitle = product.Prodtitle,
            Prodcat = product.Prodcat,
            Prodcount = product.itemcount,
            Prod_img = product.Prodimageuris?.get(0)!! ,
            Prodqty = product.Prodqty.toString() + product.Produnit.toString(),
            Prodstock = product.Prodstock,
            Prodprice = "Rs"+"${product.Prodprice}",
            admin_uid  = product.admin_uid,

            )
        lifecycleScope.launch {
            viewModel.insertcartprod(cartproduct)

        }

    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is CartListener){
            cartListener = context
        }
        else{
            throw ClassCastException("Please Implement Cart Listener")
        }
    }
}