package org.moonglass.ui

import kotlinx.css.Align
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.FlexWrap
import kotlinx.css.Grow
import kotlinx.css.JustifyContent
import kotlinx.css.JustifyItems
import kotlinx.css.LinearDimension
import kotlinx.css.TextTransform
import kotlinx.css.alignContent
import kotlinx.css.alignItems
import kotlinx.css.backgroundColor
import kotlinx.css.borderBottomWidth
import kotlinx.css.borderColor
import kotlinx.css.display
import kotlinx.css.flex
import kotlinx.css.flexDirection
import kotlinx.css.flexGrow
import kotlinx.css.flexWrap
import kotlinx.css.grow
import kotlinx.css.height
import kotlinx.css.justifyContent
import kotlinx.css.justifyItems
import kotlinx.css.marginLeft
import kotlinx.css.padding
import kotlinx.css.pct
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.textTransform
import kotlinx.css.width
import kotlinx.css.zIndex
import kotlinx.html.js.onClickFunction
import org.moonglass.ui.user.User
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.attrs
import react.dom.img
import react.setState
import styled.css
import styled.styledButton
import styled.styledDiv
import styled.styledImg
import styled.styledSpan
import kotlin.reflect.KClass

external interface NavBarState : State {
    var userMenuOpen: Boolean
    var mainMenuOpen: Boolean
}

external interface NavBarProps : Props {
    var headerComponent: KClass<out RComponent<Props, *>>?
}


class NavBar(props: NavBarProps) : RComponent<NavBarProps, NavBarState>(props) {

    override fun NavBarState.init(props: NavBarProps) {
        userMenuOpen = false
        mainMenuOpen = false
    }

    private fun closeMenus() {
        setState {
            mainMenuOpen = false
            userMenuOpen = false
        }
    }

    private fun openMain() {
        setState { mainMenuOpen = true }
    }

    private fun openUser() {
        setState { userMenuOpen = true }
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                display = Display.flex
                flexDirection = FlexDirection.row
                flexWrap = FlexWrap.wrap
                alignItems = Align.center

                backgroundColor = Color.white
                padding(1.0.rem)
                borderBottomWidth = 1.px
                borderColor = Color.lightGray
                width = 100.pct

                // keep at top of window in bigger screen modes.
                zIndex = ZIndex.NavBar()
                height = ResponsiveLayout.navBarEmHeight.rem
            }

            // left side of navbar, has icon, title and menu widget in smaller modes
            styledDiv {
                css {
                    flexGrow = 0.0
                    display = Display.flex
                    flexDirection = FlexDirection.row
                    alignItems = Align.center
                }
                styledImg(src = Theme.icon) {
                    css {
                        grow(Grow.NONE)
                        width = 2.5.rem
                    }
                }

                if (!ResponsiveLayout.current.mobile)
                    styledSpan {
                        css {
                            textTransform = TextTransform.capitalize
                            marginLeft = 1.rem
                            flex(1.0, 1.0, LinearDimension.none)
                        }
                        +Theme.title
                    }
                // menu button shown only in small layouts
                if (!ResponsiveLayout.showSideMenu) {
                    styledButton {
                        css {
                            flex(0.0, 0.0, LinearDimension.none)
                            alignContent = Align.end
                            padding(left = .5.rem, right = .5.rem)
                        }
                        img(src = "/images/menu.svg") { }
                        attrs {
                            onClickFunction = { openMain() }
                        }
                    }
                    if (state.mainMenuOpen) {
                        child(Menu::class) {
                            attrs {
                                groups = MainMenu.menu
                                style = ContextStyle(vert = 4.0, horz = 2.0)
                                dismiss = { closeMenus() }
                            }
                        }
                    }
                }
            }
            styledDiv {
                css {
                    display = Display.flex
                    flexGrow = 1.0
                    justifyItems = JustifyItems.center
                    alignItems = Align.center
                }
                props.headerComponent?.let {
                    child(it) {}
                }
            }
            styledDiv {
                css {
                    width = LinearDimension.auto
                    display = Display.flex
                    flexDirection = FlexDirection.rowReverse
                    justifyContent = JustifyContent.end
                    alignItems = Align.center
                }
                attrs {
                    onClickFunction = { openUser() }
                }
                styledImg(src = "/images/chevron_down.svg") {
                    css {
                        height = 2.rem
                        width = LinearDimension.auto
                        display = Display.inline
                        padding(left = 0.5.rem, right = 0.5.rem, top = 0.5.rem)
                    }
                }
                if (!ResponsiveLayout.current.mobile)
                    styledImg(src = "/images/profile.svg") {
                        css {
                            height = 2.rem
                            width = LinearDimension.auto
                            padding(left = 0.5.rem, right = 0.5.rem, top = 0.5.rem)
                        }
                    }
            }
        }
        // drop-down user menu
        if (state.userMenuOpen) {
            child(Menu::class) {
                attrs {
                    groups = listOf(MenuGroup("", User.menu))
                    style = ContextStyle(vert = 4.0, horz = -2.0)
                    dismiss = {
                        setState({ state ->
                            state.apply { userMenuOpen = false }
                        })
                    }
                }
            }
        }
    }
}
