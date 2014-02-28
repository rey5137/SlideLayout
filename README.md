SlideLayout
===========

SlideLayout is an Open Source Android library that allows developers to easily add SlideMenu feature. It supports 4 menu at each side of content view concurrently.

A demo app can be found on [Google PlayStore] (https://play.google.com/store/apps/details?id=com.rey.slidelayoutdemo)

![] (/screenshot/screenshot_2.png) | ![] (/screenshot/screenshot_3.png)
------------- | -------------
![] (/screenshot/screenshot_1.png)  | ![] (/screenshot/screenshot_4.png) 

I would appreciate any kind of help to improve this library. Thanks

Usage
-----

You must declare SlideLayoutStyle in styles.xml:

```xml
    <style name="SlideLayoutStyleDemo" parent="@style/SlideLayoutStyleDefault">
        <item name="dragEnable">true</item>
        <item name="leftMenuStyle">@style/LeftSlideMenuStyleDemo</item>        
        <item name="rightMenuStyle">@style/RightSlideMenuStyleDemo</item>  
        <item name="topMenuStyle">@style/TopSlideMenuStyleDemo</item>
        <item name="bottomMenuStyle">@style/BottomSlideMenuStyleDemo</item>
        <item name="menuStyle">@style/SlideMenuStyleDemo</item>
    </style>
```

######Attributes

* `dragEnable`: enable drag menu from the side of content view. If disable, can only open menu by calling function
```java
    public void openLeftMenu(boolean animation);
    
    public void openRightMenu(boolean animation);

    public void openTopMenu(boolean animation);
    
    public void openBottomMenu(boolean animation);
```

* `leftMenuStyle`: Left menu style
* `rightMenuStyle`: Right menu style
* `topMenuStyle`: Top menu style
* `bottomMenuStyle`: Bottom menu style
* `menuStyle`: Default menu style

 and some SlideMenuStyle:

```xml
    <style name="SlideMenuStyleDemo" parent="@style/SlideMenuStyleDefault">
        <item name="overDrag">false</item>
        <item name="menuBorder">48dp</item>
        <item name="menuOverDragBorder">0dp</item>
        <item name="slideRatio">0.5</item>
        <item name="menuShadow">8dp</item>
        <item name="dragEdge">48dp</item>
        <item name="touchSlop">16dp</item>
        <item name="minAlpha">0.3</item>
        <item name="velocitySlop">500</item>
        <item name="animDuration">800</item>
        <item name="closeEdge">0.75</item>
    </style>
```

######Attributes

* `overDrag`: Enable drag menu out of its size
* `menuBorder`: The size of content view visible when menu openned, can be dimension or percent of content view's size
* `menuOverDragBorder`: The size of overdrag region when menu dragged, can be dimension or percent of content view's size
* `slideRatio`: The ratio of traveling distance between menu and content view
* `menuShadow`: The size of shadow
* `dragEdge`: The size of drag region, can be dimension or -1 (match_content)
* `touchSlop`: The distance which menu start dragging
* `minAlpha`: The minimum transparency of menu 
* `velocitySlop`: The velocity of fling which menu start opening
* `animDuration`: The duration of opening and closing animation
* `closeEdge`: The distance which menu auto close when stop dragging, can be dimension or percent of menu's size

Declare the following view on your xml layout:

```xml
    <com.rey.slidelayout.SlideLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        style="@style/SlideLayoutStyleDemo"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:contentChild="4"
        app:leftMenuChild="0"
        app:rightMenuChild="1"
        app:topMenuChild="2"
        app:bottomMenuChild="3">
        
        <TextView 
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:text="Left Menu"/>
        
        <TextView 
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:text="Right Menu"/>
        
        <TextView 
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:text="Top Menu"/>
        
        <TextView 
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:text="Bottom Menu"/>
        
        <TextView 
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:text="Content View"/>
        
    </com.rey.slidelayout.SlideLayout>
```

######Attributes

* `contentChild`: index of content view in SlideLayout
* `leftMenuChild`: index of left menu in SlideLayout
* `rightMenuChild`:  index of right menu in SlideLayout
* `topMenuChild`:  index of top menu in SlideLayout
* `bottomMenuChild`:  index of bottom menu in SlideLayout

The order of menu and content view in SlideLayout decides menu will be above (Google style) or below (Facebook style) content view.

Developed By
------------

* Rey Pham - <pea5137@gmail.com>

Credits
------------
* SlideLayout is inspired by [Cyril Mottier's post](http://cyrilmottier.com/2012/05/22/the-making-of-prixing-fly-in-app-menu-part-1/ )

Contributing
------------
Want to contribute? You are welcome!
