package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class HotpatchTemplate implements IPatch {

	// handlePatch这个方法，会在应用进程启动的时候被调用，在这里来实现patch的功能
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		// 从arg0里面，可以得到主客的context供使用
		final Context context = arg0.context;
		
		// 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断		
		if (!PatchHelper.isRunInMainProcess(context)) {
			// 不是主进程就返回
			return;
		}

        // TODO 这里填上你要patch的bundle中的class名字，第三个参数是所在bundle中manifest的packageName，最后的参数为this
        Class<?> cart = PatchHelper.loadClass(context, "com.taobao.android.trade.cart.CartActivity", "com.taobao.android.trade", this);
        if (cart == null) {
            return;
        }
		// TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
		// beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
		XposedBridge.findAndHookMethod(cart, "setBottomViewVisibility", int.class,
				new XC_MethodHook() {
					// 这个方法执行的相当于在原oncreate方法后面，加上一段逻辑。
					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						Activity instance = (Activity) param.thisObject;
                        Object view = XposedHelpers.callMethod(instance, "findViewById", com.taobao.android.trade.R.id.cart_delelte_layout);
                        Object color = XposedHelpers.callStaticMethod(Color.class, "parseColor", "#ff00ff00");
                        XposedHelpers.callMethod(view,"setBackgroundColor",color);
					}
				});




        Class<?> cartClosingCostView = PatchHelper.loadClass(context, "com.taobao.android.trade.cart.ui.view.CartClosingCostView", "com.taobao.android.trade", this);
        if (cartClosingCostView == null) {
            return;
        }
        XposedBridge.findAndHookMethod(cartClosingCostView, "init", null,
                new XC_MethodHook() {
                    // 这个方法执行的相当于在原oncreate方法后面，加上一段逻辑。
                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        Activity instance = (Activity) param.thisObject;
                        Object view = XposedHelpers.callMethod(instance, "findViewById", com.taobao.android.trade.R.id.cart_btn_charge);
                        Object color = XposedHelpers.callStaticMethod(Color.class, "parseColor", "#ff00ff00");
                        XposedHelpers.callMethod(view,"setBackgroundColor",color);
                    }
                });

    }
}
