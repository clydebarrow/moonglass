/*
 * Copyright (c) 2021. Clyde Stubbs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.moonglass.ui

import kotlinx.css.Align
import kotlinx.css.CssBuilder
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.FlexWrap
import kotlinx.css.Grow
import kotlinx.css.Image
import kotlinx.css.JustifyContent
import kotlinx.css.JustifyItems
import kotlinx.css.LinearDimension
import kotlinx.css.Position
import kotlinx.css.QuotedString
import kotlinx.css.TextTransform
import kotlinx.css.alignContent
import kotlinx.css.alignItems
import kotlinx.css.backgroundColor
import kotlinx.css.backgroundImage
import kotlinx.css.borderBottomWidth
import kotlinx.css.borderColor
import kotlinx.css.borderRadius
import kotlinx.css.color
import kotlinx.css.content
import kotlinx.css.display
import kotlinx.css.flex
import kotlinx.css.flexDirection
import kotlinx.css.flexGrow
import kotlinx.css.flexWrap
import kotlinx.css.grow
import kotlinx.css.height
import kotlinx.css.justifyContent
import kotlinx.css.justifyItems
import kotlinx.css.left
import kotlinx.css.margin
import kotlinx.css.marginLeft
import kotlinx.css.opacity
import kotlinx.css.padding
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.properties.deg
import kotlinx.css.properties.rotate
import kotlinx.css.properties.transform
import kotlinx.css.properties.transition
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.right
import kotlinx.css.textTransform
import kotlinx.css.top
import kotlinx.css.width
import kotlinx.css.zIndex
import kotlinx.html.DIV
import org.moonglass.ui.api.Api
import org.moonglass.ui.api.gravatarUrl
import org.moonglass.ui.api.user
import org.moonglass.ui.user.User
import org.moonglass.ui.utility.StateVar
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.attrs
import react.dom.onClick
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv
import styled.styledImg
import styled.styledSpan

external interface NavBarState : State {
}

external interface NavBarProps : Props {
    var api: Api
    var renderWidget: ((RBuilder) -> Unit)?
    var isSideBarShowing: StateVar<Boolean>
}


class NavBar(props: NavBarProps) : RComponent<NavBarProps, NavBarState>(props) {

    override fun NavBarState.init(props: NavBarProps) {
    }

    private fun openUser() {
        App.showContextMenu(ContextMenuData(listOf(MenuGroup("", User.menu)), ContextStyle(vert = 4.0, horz = -2.0)))
    }

    private fun StyledDOMBuilder<DIV>.hamburger(n: Int, block: CssBuilder.() -> Unit) {
        styledSpan {

            css {
                put("transform-origin", "left center")
                position = Position.relative
                height = 20.pct
                backgroundColor = Theme().content.textColor
                top = 20.pct * n * 2
                left = 0.px
                right = 0.px
                borderRadius = 3.px
                transition("all", ResponsiveLayout.menuTransitionTime)
                display = Display.block
                block()
            }
        }
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                display = Display.flex
                flexDirection = FlexDirection.row
                flexWrap = FlexWrap.wrap
                alignItems = Align.center

                backgroundColor = Theme().content.backgroundColor
                padding(0.5.rem)
                borderBottomWidth = 1.px
                borderColor = Theme().borderColor

                // keep at top of window
                position = Position.fixed
                left = 0.px
                top = 0.px
                right = 0.px
                height = ResponsiveLayout.navBarEmHeight
                zIndex = ZIndex.NavBar()
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

                styledSpan {
                    attrs {
                        onClick = { props.isSideBarShowing.value = !props.isSideBarShowing() }
                    }
                    css {
                        if (ResponsiveLayout.current.mobile)
                            display = Display.none
                        color = Theme().content.textColor
                        textTransform = TextTransform.capitalize
                        marginLeft = 1.rem
                        flex(1.0, 1.0, LinearDimension.none)
                    }
                    +Theme.title
                }
                // menu button shown only in small layouts
                styledDiv {
                    name = "menuToggler"
                    css {
                        if (ResponsiveLayout.showSideMenu)
                            display = Display.none
                        height = 1.0.rem
                        width = 1.5.rem
                        flex(0.0, 0.0, LinearDimension.none)
                        alignContent = Align.end
                        margin(left = 1.rem, right = 1.rem)
                    }
                    hamburger(0) {
                        if (props.isSideBarShowing()) {
                            width = 115.pct             // this value determined by trial and error.
                            transform {
                                rotate(45.deg)
                            }

                        }
                    }
                    hamburger(1) {
                        if (props.isSideBarShowing())
                            opacity = 0.0
                    }
                    hamburger(2) {
                        if (props.isSideBarShowing()) {
                            width = 115.pct
                            transform {
                                rotate((-45).deg)
                            }
                        }
                    }
                    attrs {
                        onClick = { props.isSideBarShowing.value = !props.isSideBarShowing() }
                    }
                }
            }
            styledDiv {
                name = "widgetWrapper"
                css {
                    display = Display.flex
                    flexGrow = 1.0
                    justifyItems = JustifyItems.center
                    alignItems = Align.center
                    marginLeft = 2.rem
                }
                props.renderWidget?.invoke(this)
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
                    onClick = { openUser() }
                }
                val imgSrc = props.api.user?.gravatarUrl
                styledImg {
                    attrs {
                        if (imgSrc != null)
                            src = imgSrc
                        else
                            src = "/images/profile.svg"
                    }
                    css {
                        before {
                            content = QuotedString(" ")
                            display = Display.block
                            position = Position.absolute
                            height = 3.rem
                            width = 3.rem
                            backgroundImage = Image("/images/profile.svg")
                        }
                        height = 3.rem
                        display = Display.flex
                        alignContent = Align.start
                        width = LinearDimension.auto
                        margin(right = 0.5.rem)
                        if (imgSrc != null)
                            borderRadius = 25.pct
                        padding(2.px)
                    }
                }
            }
        }
    }
}
