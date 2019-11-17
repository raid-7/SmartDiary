package ru.raid.smartdiary

import android.Manifest
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_avatar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.raid.smartdiary.db.AppDatabase
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private fun clamp(value: Int, minV: Int, maxV: Int) =
        max(min(value, maxV), minV)

class AvatarFragment : PermissionHelperFragment<PermissionTag>(PermissionTag.values()) {
    private var bubbleVisible = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_avatar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val metaDao = AppDatabase.getInstance(context!!).metaDao()
        metaDao.getLiveMeta(ru.raid.smartdiary.db.Metadata.AVATAR_LEVEL).observe(::getLifecycle) {
            val level = clamp(it?.toInt() ?: 0, 0, AVATARS.size - 1)
            avatarImageView.setImageResource(AVATARS[level])
        }

        avatarBubble.setOnClickListener {
            setBubbleVisible(visible = false, makeTransition = false)
            askForTalk()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!bubbleVisible)
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                (activity as? MainActivity)?.let {
                    it.talkIntentionManager.waitForBubble()
                    showBubble()
                }
            }
    }

    override fun onPermissionsResult(tag: PermissionTag, granted: Boolean) {
        if (tag == PermissionTag.ASK_FOR_TALK && granted) {
            val mainActivity = activity as? MainActivity
            mainActivity?.askForTalk()
        }
    }

    private fun askForTalk() {
        withPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                R.string.microphone_rationale,
                R.string.microphone_rationale_in_settings,
                PermissionTag.ASK_FOR_TALK
        )
    }

    private fun showBubble() {
        setBubbleVisible(true)
    }

    private fun setBubbleVisible(visible: Boolean, makeTransition: Boolean = true) {
        if (bubbleVisible == visible)
            return

        val view = view as? ViewGroup ?: return
        if (makeTransition)
            TransitionManager.beginDelayedTransition(view)
        avatarBubble.layoutParams = avatarBubble.layoutParams.apply {
            width = if (visible) {
                resources.getDimension(R.dimen.bubble_open)
            } else {
                resources.getDimension(R.dimen.bubble_close)
            }.roundToInt()
        }
        bubbleVisible = visible
    }

    companion object {
        private val AVATARS = arrayOf(
                R.drawable.avatar_1,
                R.drawable.avatar_2,
                R.drawable.avatar_3,
                R.drawable.avatar_4,
                R.drawable.avatar_5,
                R.drawable.avatar_6,
                R.drawable.avatar_7
        )
    }
}
