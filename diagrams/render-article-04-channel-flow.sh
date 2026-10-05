#!/bin/zsh
set -euo pipefail

script_dir="${0:A:h}"
frames_dir="$(mktemp -d "${TMPDIR%/}/buildpulse-channel-flow.XXXXXX")"
trap 'rm -rf -- "$frames_dir"' EXIT
regular_font="/System/Library/Fonts/Supplemental/Arial.ttf"
bold_font="/System/Library/Fonts/Supplemental/Arial Bold.ttf"

status_fill() {
    case "$1" in
        RUNNING) echo '#EEF4FF' ;;
        COMPLETE) echo '#E8F8F4' ;;
        CANCELLED) echo '#FDECEC' ;;
        *) echo '#FFFFFF' ;;
    esac
}

status_stroke() {
    case "$1" in
        RUNNING) echo '#6172F3' ;;
        COMPLETE) echo '#087E8B' ;;
        CANCELLED) echo '#D92D20' ;;
        *) echo '#98A2B3' ;;
    esac
}

render_frame() {
    local frame_number="$1"
    local compiler_state="$2"
    local tests_state="$3"
    local security_state="$4"
    local active_sender="$5"
    local received_count="$6"
    local channel_state="$7"
    local caption="$8"
    local output="$frames_dir/frame-${frame_number}.png"
    local compiler_fill="$(status_fill "$compiler_state")"
    local tests_fill="$(status_fill "$tests_state")"
    local security_fill="$(status_fill "$security_state")"
    local compiler_stroke="$(status_stroke "$compiler_state")"
    local tests_stroke="$(status_stroke "$tests_state")"
    local security_stroke="$(status_stroke "$security_state")"
    local compiler_arrow='#D0D5DD'
    local tests_arrow='#D0D5DD'
    local security_arrow='#D0D5DD'
    local collector_arrow='#D0D5DD'
    local channel_fill='#FFFFFF'
    local channel_stroke='#6172F3'

    [[ "$active_sender" == 'COMPILER' ]] && compiler_arrow='#6172F3'
    [[ "$active_sender" == 'TESTS' ]] && tests_arrow='#6172F3'
    [[ "$active_sender" == 'SECURITY' ]] && security_arrow='#6172F3'
    [[ "$active_sender" != 'NONE' ]] && collector_arrow='#12B3A8'
    [[ "$channel_state" == 'ACTIVE' ]] && channel_fill='#F4F3FF'
    [[ "$channel_state" == 'COMPLETE' ]] && { channel_fill='#E8F8F4'; channel_stroke='#087E8B'; }
    [[ "$channel_state" == 'CANCELLED' ]] && { channel_fill='#FDECEC'; channel_stroke='#D92D20'; }

    magick \
        -size 1200x675 xc:'#F7F4ED' \
        -font "$bold_font" -fill '#101828' -pointsize 42 -gravity NorthWest \
        -annotate +60+34 'CHANNELFLOW: THREE PRODUCERS. ONE FLOW.' \
        -font "$regular_font" -fill '#475467' -pointsize 21 \
        -annotate +60+90 'One collection launches structured children. Every child uses send().' \
        -fill "$compiler_fill" -stroke "$compiler_stroke" -strokewidth 3 \
        -draw 'roundrectangle 60,155 325,245 18,18' \
        -fill "$tests_fill" -stroke "$tests_stroke" \
        -draw 'roundrectangle 60,270 325,360 18,18' \
        -fill "$security_fill" -stroke "$security_stroke" \
        -draw 'roundrectangle 60,385 325,475 18,18' \
        -font "$bold_font" -fill '#101828' -stroke none -pointsize 19 \
        -annotate +86+181 'COMPILER' \
        -annotate +86+296 'TEST RUNNER' \
        -annotate +86+411 'SECURITY SCANNER' \
        -font "$regular_font" -fill '#475467' -pointsize 16 \
        -annotate +86+214 "$compiler_state" \
        -annotate +86+329 "$tests_state" \
        -annotate +86+444 "$security_state" \
        -fill "$channel_fill" -stroke "$channel_stroke" -strokewidth 3 \
        -draw 'roundrectangle 465,225 755,420 22,22' \
        -font "$bold_font" -fill '#101828' -stroke none -pointsize 26 \
        -annotate +510+262 'channelFlow' \
        -font "$regular_font" -fill '#475467' -pointsize 18 \
        -annotate +518+312 'launch { ... }' \
        -annotate +518+347 'send(report)' \
        -font "$bold_font" -fill "$channel_stroke" -pointsize 17 \
        -annotate +518+384 "$channel_state" \
        -fill '#FFFFFF' -stroke '#087E8B' -strokewidth 3 \
        -draw 'roundrectangle 875,245 1140,400 22,22' \
        -font "$bold_font" -fill '#101828' -stroke none -pointsize 22 \
        -annotate +932+280 'COLLECTOR' \
        -font "$regular_font" -fill '#475467' -pointsize 18 \
        -annotate +915+325 'Receives by arrival' \
        -font "$bold_font" -fill '#087E8B' -pointsize 19 \
        -annotate +930+365 "REPORTS: ${received_count}" \
        -stroke "$compiler_arrow" -strokewidth 7 -fill none \
        -draw 'line 330,200 450,272' \
        -fill "$compiler_arrow" -stroke none -draw 'polygon 450,272 429,270 439,254' \
        -stroke "$tests_arrow" -strokewidth 7 -fill none \
        -draw 'line 330,315 450,322' \
        -fill "$tests_arrow" -stroke none -draw 'polygon 450,322 431,311 431,333' \
        -stroke "$security_arrow" -strokewidth 7 -fill none \
        -draw 'line 330,430 450,372' \
        -fill "$security_arrow" -stroke none -draw 'polygon 450,372 438,389 429,373' \
        -stroke "$collector_arrow" -strokewidth 7 -fill none \
        -draw 'line 760,322 860,322' \
        -fill "$collector_arrow" -stroke none -draw 'polygon 860,322 841,311 841,333' \
        -fill '#FFFFFF' -stroke '#D0D5DD' -strokewidth 2 \
        -draw 'roundrectangle 60,505 1140,610 18,18' \
        -font "$bold_font" -fill '#344054' -stroke none -pointsize 15 \
        -annotate +80+527 'MERGED ARRIVAL ORDER' \
        -font "$regular_font" -pointsize 15 -fill '#475467' \
        -annotate +80+558 '1  Compiler: RUNNING' \
        -annotate +405+558 '2  Tests: RUNNING' \
        -annotate +710+558 '3  Security: RUNNING' \
        -annotate +80+588 '4  Compiler: COMPLETE' \
        -annotate +405+588 '5  Security: COMPLETE' \
        -annotate +710+588 '6  Tests: COMPLETE' \
        -fill '#101828' -stroke none \
        -draw 'roundrectangle 60,625 1140,665 12,12' \
        -font "$bold_font" -fill '#FFFFFF' -pointsize 17 -gravity Center \
        -annotate +0+307 "$caption" \
        "$output"
}

render_frame 00 IDLE IDLE IDLE NONE 0 IDLE 'Flow exists, but no collect(): no producer runs.'
render_frame 01 RUNNING RUNNING RUNNING NONE 0 ACTIVE 'collect() starts one cold execution and launches three child coroutines.'
render_frame 02 RUNNING RUNNING RUNNING COMPILER 1 ACTIVE 'Compiler sends RUNNING. Collector receives report 1.'
render_frame 03 RUNNING RUNNING RUNNING TESTS 2 ACTIVE 'Tests send RUNNING. Collector receives report 2.'
render_frame 04 RUNNING RUNNING RUNNING SECURITY 3 ACTIVE 'Security sends RUNNING. Collector receives report 3.'
render_frame 05 COMPLETE RUNNING RUNNING COMPILER 4 ACTIVE 'Compiler finishes first. Arrival order is not producer-list order.'
render_frame 06 COMPLETE RUNNING COMPLETE SECURITY 5 ACTIVE 'Security finishes next while tests keep running.'
render_frame 07 COMPLETE COMPLETE COMPLETE TESTS 6 COMPLETE 'Tests finish last. channelFlow completes after every child completes.'
render_frame 08 CANCELLED CANCELLED CANCELLED NONE 0 CANCELLED 'If collection cancels, the channelFlow scope cancels every child.'

magick "$frames_dir/frame-07.png" "$script_dir/article-04-channel-flow.png"

magick \
    -delay 160 "$frames_dir/frame-00.png" \
    -delay 145 "$frames_dir/frame-01.png" \
    -delay 115 "$frames_dir/frame-02.png" \
    -delay 115 "$frames_dir/frame-03.png" \
    -delay 115 "$frames_dir/frame-04.png" \
    -delay 125 "$frames_dir/frame-05.png" \
    -delay 125 "$frames_dir/frame-06.png" \
    -delay 185 "$frames_dir/frame-07.png" \
    -delay 210 "$frames_dir/frame-08.png" \
    -loop 0 -layers Optimize "$script_dir/article-04-channel-flow.gif"
