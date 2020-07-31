package org.covidwatch.android.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.covidwatch.android.R
import org.covidwatch.android.exposurenotification.Failure
import org.covidwatch.android.extension.observeEvent

abstract class BaseViewModelFragment<T : ViewBinding, VM : BaseViewModel> : BaseFragment<T>() {

    protected abstract val viewModel: VM

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {
            observeEvent(status) {
                handleStatus(it)
            }
            observeEvent(resolvable) { resolvable ->
                resolvable.apiException.status.startResolutionForResult(
                    activity, resolvable.requestCode
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        lifecycleScope.launch {
            viewModel.handleResolution(requestCode, resultCode)
        }
    }

    protected fun handleStatus(it: Failure) {
        when (it) {
            Failure.EnStatus.FailedDiskIO -> {
                val snackbar = Snackbar.make(
                    binding.root,
                    R.string.insufficient_storage,
                    BaseTransientBottomBar.LENGTH_INDEFINITE
                )
                snackbar.setAction(R.string.ok) { snackbar.dismiss() }
                snackbar.show()
            }
            Failure.EnStatus.Failed -> {
                Toast.makeText(
                    context,
                    R.string.unknown_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
            Failure.EnStatus.NotSupported -> {
                // TODO: 01.08.2020 Rework to a dialog
                val snackbar = Snackbar.make(
                    binding.root,
                    R.string.notification_en_not_supported,
                    BaseTransientBottomBar.LENGTH_INDEFINITE
                )
                snackbar.setAction("Update") {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(
                            "https://play.google.com/store/apps/details?id=com.google.android.gms"
                        )
                        setPackage("com.android.vending")
                    }
                    startActivity(intent)
                }
                snackbar.show()
            }
            Failure.EnStatus.Unauthorized -> {
                Toast.makeText(
                    context,
                    R.string.notification_app_unauthorized,
                    Toast.LENGTH_LONG
                ).show()
            }
            Failure.NetworkError -> {
                Toast.makeText(
                    context,
                    R.string.no_connection_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
            Failure.ServerError -> {
                Toast.makeText(
                    context,
                    R.string.server_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
            Failure.DeviceAttestation -> {
                Toast.makeText(
                    context,
                    R.string.device_attestation_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
