package com.pentabytex.alshafimedledger.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.pentabytex.alshafimedledger.R
import com.pentabytex.alshafimedledger.adapter.QuickAccessAdapter
import com.pentabytex.alshafimedledger.data.models.User
import com.pentabytex.alshafimedledger.databinding.FragmentHomeBinding
import com.pentabytex.alshafimedledger.enums.DashboardTitle
import com.pentabytex.alshafimedledger.ui.activities.AddCustomerActivity
import com.pentabytex.alshafimedledger.ui.activities.AddMedicineActivity
import com.pentabytex.alshafimedledger.utils.Utils.navigateToActivity
import com.pentabytex.alshafimedledger.viewmodels.DashboardViewModel
import com.pentabytex.alshafimedledger.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private lateinit var quickAccessAdapter: QuickAccessAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpListeners()
        observeViewModel()
        fetchDataFromViewModels()
        setupQuickButtonRecyclerView()
    }


    private fun fetchDataFromViewModels() {
        val uid = userViewModel.userState.value?.uid
        uid?.let { userViewModel.fetchUserProfile(it) }
    }

    private fun populateUserData(user: User) {
        if (_binding == null) return

        binding.welcomeText.text = "Hello, ${user.name}!"
    }


    private fun setUpListeners() {
        binding.profileImage.setOnClickListener {
//            navigateToActivity(requireContext(), ProfileActivity::class.java, isAnimation = true)
        }
    }

    private fun observeViewModel() {

        // Observe User Profile State
        lifecycleScope.launch {
            userViewModel.profileState.collectLatest { user ->
                user?.let {
                    populateUserData(it)
                    shimmerVisibility()
                }
            }
        }

        dashboardViewModel.quickAccessButtons.observe(viewLifecycleOwner) { buttons ->
            quickAccessAdapter.submitList(buttons)
        }

    }

    private fun shimmerVisibility() {
        binding.apply {
            fullScreenShimmer.visibility = View.VISIBLE
            mainContent.visibility = View.GONE

            fullScreenShimmer.stopShimmer()
            fullScreenShimmer.visibility = View.GONE
            mainContent.visibility = View.VISIBLE
        }
    }

    private fun setupQuickButtonRecyclerView() {

        binding.quickAccessRecyclerView.layoutManager = GridLayoutManager(requireContext(), 4)

        quickAccessAdapter = QuickAccessAdapter { title ->
            when (DashboardTitle.entries.find { it.title == title }) {
                DashboardTitle.AddMedicines-> {
                    navigateToActivity(requireContext(), AddMedicineActivity::class.java, isAnimation = true)
                }
                DashboardTitle.ViewMedicines -> {
                   findNavController().navigate(R.id.action_homeFragment_to_medicinesFragment)
                }
                DashboardTitle.AddCustomers -> {
                    navigateToActivity(requireContext(), AddCustomerActivity::class.java, isAnimation = true)
                }
                DashboardTitle.ViewCustomers -> {
                    findNavController().navigate(R.id.action_homeFragment_to_customersFragment)
                }
                DashboardTitle.SalesHistory -> {
                    findNavController().navigate(R.id.action_homeFragment_to_salesFragment)
                }

                else -> {
                    /* Handle unknown cases */
                }
            }
        }

        binding.quickAccessRecyclerView.adapter = quickAccessAdapter
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}